#!/usr/bin/env bash
#
# Dokja dev stack manager — thin wrapper around
#   docker compose -f infra/compose.dev.yaml
#
# Works on Linux, macOS, and Windows Git Bash. When Docker is hosted inside
# WSL2 (not Docker Desktop), every docker invocation is wrapped in `wsl` and
# the compose file path is converted to its /mnt/<drive> form so relative
# mounts (./keycloak, ./otel, ../backend) resolve correctly.
#
# Environment:
#   DOKJA_DOCKER=native|wsl|auto   Force the docker host (default: auto)

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE_FILE="$SCRIPT_DIR/compose.dev.yaml"

DOCKER=(docker)
COMPOSE_OPTS=(-f "$COMPOSE_FILE")

# Color support (disable with NO_COLOR, auto-disabled when piped)
if [[ -t 1 ]] && [[ -z "${NO_COLOR:-}" ]]; then
  C_BOLD=$'\033[1m'
  C_UNDERLINE=$'\033[4m'
  C_DIM=$'\033[2m'
  C_RED=$'\033[31m'
  C_YELLOW=$'\033[33m'
  C_CYAN=$'\033[36m'
  C_RESET=$'\033[0m'
else
  C_BOLD="" C_UNDERLINE="" C_DIM="" C_RED="" C_YELLOW="" C_CYAN="" C_RESET=""
fi

die() {
  printf '%serror:%s %s\n' "$C_RED" "$C_RESET" "$1" >&2
  exit 1
}

warn() {
  printf '%swarning:%s %s\n' "$C_YELLOW" "$C_RESET" "$1" >&2
}

print_cmd() {
  local name="$1" args="$2" desc="$3"
  local pad=$((26 - ${#name} - ${#args} - 1))
  printf '  %s%s %s%s%s%*s%s\n' "$C_BOLD" "$name" "$C_CYAN" "$args" "$C_RESET" "$pad" "" "$desc"
}

print_profile() {
  local name="$1" desc="$2"
  local pad=$((8 - ${#name}))
  printf '  %s%s%s%*s%s\n' "$C_BOLD" "$name" "$C_RESET" "$pad" "" "$desc"
}

usage() {
  printf '%s\n' "${C_BOLD}${C_UNDERLINE}Usage:${C_RESET} ${C_BOLD}dokja-dev.sh${C_RESET} <command> [options]"
  printf '\n'
  printf '%s\n' "${C_BOLD}Commands:${C_RESET}"
  print_cmd "up|start" "[api|obs|full]" "Start dependencies (+ profile services)"
  print_cmd "stop" "[service...]" "Stop all containers or the given services"
  print_cmd "down" "[-v]" "Tear down the stack (-v removes volumes)"
  print_cmd "restart" "[service...]" "Restart, keeping the active profile"
  print_cmd "status" "" "Show container status (docker compose ps)"
  print_cmd "logs" "[--no-follow] [svc]" "Tail logs (--no-follow prints once)"
  print_cmd "build" "[service...]" "Build images (default: all with a build context)"
  print_cmd "help" "" "Show this help"
  printf '\n'
  printf '%s\n' "${C_BOLD}Profiles:${C_RESET}"
  print_profile "api" "api service only (dev profile)"
  print_profile "obs" "otel-collector + openobserve"
  print_profile "full" "api + observability (dev,dev-obs)"
  printf '\n'
  printf '%s\n' "${C_BOLD}Environment:${C_RESET}"
  printf '  %sDOKJA_DOCKER%s=%s%s%s   %s\n' \
    "$C_BOLD" "$C_RESET" "$C_CYAN" "native|wsl|auto" "$C_RESET" "Docker host to use (default: auto)"
}

wsl_compose_file() {
  local p="$COMPOSE_FILE"
  if command -v cygpath >/dev/null 2>&1; then
    p="$(cygpath -w "$COMPOSE_FILE")"
  fi
  wsl wslpath -a -u "${p//\\//}"
}

use_wsl_docker() {
  local wsl_file
  wsl_file="$(wsl_compose_file)"
  export MSYS_NO_PATHCONV=1 MSYS2_ARG_CONV_EXCL="*"
  DOCKER=(wsl docker)
  COMPOSE_OPTS=(-f "$wsl_file")
  printf '%sdocker:%s using WSL host (%s%s%s)\n' "$C_CYAN" "$C_RESET" "$C_DIM" "$wsl_file" "$C_RESET" >&2
}

resolve_docker() {
  local mode="${DOKJA_DOCKER:-auto}"
  case "$mode" in
    native|wsl|auto) : ;;
    *) die "unknown DOKJA_DOCKER '$mode' (expected native|wsl|auto)" ;;
  esac
  local os on_windows=false
  os="$(uname -s)"
  case "$os" in
    MINGW*|MSYS*|CYGWIN*) on_windows=true ;;
  esac

  local native_ok=false native_bind_ok=false wsl_ok=false
  if docker compose version >/dev/null 2>&1; then
    native_ok=true
    case "$(docker info --format '{{.OperatingSystem}}' 2>/dev/null || true)" in
      *"Docker Desktop"*) native_bind_ok=true ;;
    esac
  fi
  if [[ "$on_windows" == true ]] && command -v wsl >/dev/null 2>&1; then
    if wsl docker compose version >/dev/null 2>&1; then
      wsl_ok=true
    fi
  fi

  case "$mode" in
    native)
      [[ "$native_ok" == true ]] || die "docker compose not available natively (try DOKJA_DOCKER=wsl)"
      ;;
    wsl)
      [[ "$wsl_ok" == true ]] || die "docker not reachable via WSL (try DOKJA_DOCKER=native)"
      use_wsl_docker
      ;;
    auto|*)
      if [[ "$native_bind_ok" == true ]]; then
        :
      elif [[ "$wsl_ok" == true ]]; then
        use_wsl_docker
      elif [[ "$native_ok" == true ]]; then
        warn "daemon reports no Windows bind-mount support; use DOKJA_DOCKER=wsl if mounts fail"
      else
        die "no usable docker found (tried native and WSL)"
      fi
      ;;
  esac
}

existing_profiles() {
  local out
  out="$("${DOCKER[@]}" compose "${COMPOSE_OPTS[@]}" ps -a --format json 2>/dev/null || true)"
  local profiles=()
  [[ "$out" == *'"Service":"api-full"'* ]] && profiles+=('full')
  [[ "$out" == *'"Service":"api"'* ]] && profiles+=('api')
  if [[ "$out" == *'"Service":"otel-collector"'* || "$out" == *'"Service":"openobserve"'* ]]; then
    profiles+=('obs')
  fi
  printf '%s\n' "${profiles[*]}"
}

profile_flags() {
  local p flags=()
  for p in $1; do
    case "$p" in
      api|obs|full) flags+=(--profile "$p") ;;
      *) die "refusing unsafe profile name '$p'" ;;
    esac
  done
  ((${#flags[@]})) && printf '%s\0' "${flags[@]}"
}

cmd_up() {
  local profile="" args=()
  while (($#)); do
    case "$1" in
      api|obs|full)
        [[ -z "$profile" ]] || die "profile already given"
        profile="$1"
        ;;
      *)
        args+=("$1")
        ;;
    esac
    shift
  done
  local prof_flag=()
  [[ -n "$profile" ]] && prof_flag+=(--profile "$profile")
  "${DOCKER[@]}" compose "${prof_flag[@]}" "${COMPOSE_OPTS[@]}" up -d "${args[@]}"
}

cmd_stop() {
  local flags=()
  mapfile -d '' flags < <(profile_flags "$(existing_profiles)")
  "${DOCKER[@]}" compose "${flags[@]}" "${COMPOSE_OPTS[@]}" stop "$@"
}

cmd_down() {
  local flags=()
  mapfile -d '' flags < <(profile_flags "$(existing_profiles)")
  "${DOCKER[@]}" compose "${flags[@]}" "${COMPOSE_OPTS[@]}" down "$@"
}

cmd_restart() {
  local flags=()
  mapfile -d '' flags < <(profile_flags "$(existing_profiles)")
  "${DOCKER[@]}" compose "${flags[@]}" "${COMPOSE_OPTS[@]}" stop "$@"
  "${DOCKER[@]}" compose "${flags[@]}" "${COMPOSE_OPTS[@]}" up -d "$@"
}

cmd_status() {
  "${DOCKER[@]}" compose "${COMPOSE_OPTS[@]}" ps "$@"
}

cmd_logs() {
  local follow="-f" args=()
  for a in "$@"; do
    if [[ "$a" == "--no-follow" ]]; then
      follow=""
    else
      args+=("$a")
    fi
  done
  "${DOCKER[@]}" compose "${COMPOSE_OPTS[@]}" logs $follow "${args[@]}"
}

cmd_build() {
  "${DOCKER[@]}" compose "${COMPOSE_OPTS[@]}" build "$@"
}

main() {
  local cmd="${1:-}"
  if [[ -z "$cmd" ]]; then
    usage >&2
    exit 1
  fi
  shift
  case "$cmd" in
    help|-h|--help)
      usage
      return 0
      ;;
  esac
  resolve_docker
  case "$cmd" in
    up|start) cmd_up "$@" ;;
    stop) cmd_stop "$@" ;;
    down) cmd_down "$@" ;;
    restart) cmd_restart "$@" ;;
    status|ps) cmd_status "$@" ;;
    logs) cmd_logs "$@" ;;
    build) cmd_build "$@" ;;
    *) die "unknown command '$cmd' (see help)" ;;
  esac
}

main "$@"
