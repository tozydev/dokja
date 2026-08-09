# PRODUCT REQUIREMENTS DOCUMENT (PRD): DOKJA PLATFORM

---

- **Project:** DOKJA Platform
- **Author:** tozydev
- **Created:** 2026-08-08
- **Updated:** 2026-08-09
- **Version:** v1.2

### Revision History

| Version | Date       | Author  | Change Description                                                                                                                                                                                            |
| ------- | ---------- | ------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| v1.0    | 2026-08-08 | tozydev | Initial version of the DOKJA Platform PRD document                                                                                                                                                            |
| v1.1    | 2026-08-09 | tozydev | Removed F-WALLET-04 & reindexed F-WALLET; aligned Reference columns; added NFR-09 Observability & NFR-10 Audit logging.                                                                                       |
| v1.2    | 2026-08-09 | tozydev | Indexed headings (1–7); clarified BR-4.2/F-COMMON-04 to block simultaneous content consumption; shortened Out Of Scope; added NFR-11 & NFR-12; renamed Product Overview to Components; simplified Milestones. |

---

## 1. Overview

DOKJA is an online digital content distribution platform for the Vietnam market, connecting 3
content pillars: **Anime – Comic (Webtoon) – Novel** under the **Direct Publisher & Distributor**
model.

Problems to be solved:

- Users lack a centralized platform to experience Anime, Comic, and Novel simultaneously with
  officially licensed, high-quality, and copyright-cleared content.
- The need to read/watch across multiple devices (Web, Mobile), personalized experience, and
  context-aware (AI) search is not fully met by existing platforms.

Core values:

- Seamless experience: read comics, watch anime, and read novels on the same account.
- Flexible monetization: Freemium, Subscription, Coin, promotional events.
- AI system supporting search, recommendation, contextual Q&A, and roleplay with characters.
- Compliance with age classification (P/13+/16+/18+) and data security.

## 2. Business Rules

Business rules are grouped into 8 domains. Each rule has an identifier (BR-xx), a concise
description, and its scope of application. Parameters have **configurable defaults** set by the
System Admin.

### BR-1. Coin Wallet

| Code   | Rule                            | Detail                                                                                                                                               |
| ------ | ------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------- |
| BR-1.1 | **Paid Coin never expires**     | Coin purchased with real money has no expiration date.                                                                                               |
| BR-1.2 | **Bonus Coin expires**          | Coin received from events/promotions or Coin Bundle packages expires after **30 days** from receipt (configurable default).                          |
| BR-1.3 | **Coin deduction order**        | When paying with Coin, the system deducts Bonus Coin first; within Bonus Coin, coins with the nearest expiration are deducted first, then Paid Coin. |
| BR-1.4 | **Permanent ownership by Coin** | Users only **permanently own** a chapter/episode when purchasing with Coin (paid/bonus).                                                             |

### BR-2. Subscription Plans

| Code   | Rule                        | Detail                                                                                                                                                                                                                                                            |
| ------ | --------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| BR-2.1 | **Standard Plan**           | Watch Anime in HD/4K (ad-free) and access all content of titles in the plan's supported list.                                                                                                                                                                     |
| BR-2.2 | **Coin Bundle Plan**        | Includes all Standard Plan benefits, plus a monthly recurring amount of Bonus Coin at a discounted price.                                                                                                                                                         |
| BR-2.3 | **Unlimited access**        | Access is determined **by title** (not by chapter/episode): while the subscription is active, the user gets **unlimited access to the number of chapters/episodes** of titles in the plan; if a title is removed from the plan → access to that title is revoked. |
| BR-2.4 | **Revoke access on expiry** | When a subscription expires, plan-based access is revoked; chapters already purchased with Coin retain permanent ownership.                                                                                                                                       |
| BR-2.5 | **Plan configuration**      | Operation Admin is allowed to create/edit plans (name, price, supported title list, Bonus Coin amount, duration) and change the title list per campaign.                                                                                                          |
| BR-2.6 | **Plan terms & discounts**  | Operation Admin creates/configures plan terms themselves (no hardcoded limited list) with price and discount per term; longer terms are encouraged to receive higher discounts.                                                                                   |
| BR-2.7 | **No auto-renewal**         | The system does **not support auto-renewal**; when a plan expires, access is revoked until the user actively purchases again.                                                                                                                                     |

### BR-3. Freemium Content

| Code   | Rule                          | Detail                                                                                                                                                                                      |
| ------ | ----------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| BR-3.1 | **Always Free**               | Each title can have one or more chapters/episodes marked Always Free; there is no limit on the number.                                                                                      |
| BR-3.2 | **Configuration permissions** | Content Manager configures the number of Always Free chapters/episodes per title.                                                                                                           |
| BR-3.3 | **Access audience & ads**     | Always Free content is available to **all users** (Guest and Reader, paid or not) and **shows no advertisements** — unlike chapters unlocked from events which have ads (ad-watch, BR-5.2). |
| BR-3.4 | **Temporary free (campaign)** | Operation Admin marks chapters/episodes temporarily free during a campaign; once the campaign ends they automatically revert to paid status.                                                |

### BR-4. Devices & Accounts

| Code   | Rule                                       | Detail                                                                                                                                                                                                                                                                                                                                                         |
| ------ | ------------------------------------------ | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| BR-4.1 | **Login device limit**                     | An account can be logged in on a maximum of **3 devices** (Mobile/Tablet/Web) — configurable default.                                                                                                                                                                                                                                                          |
| BR-4.2 | **Block simultaneous content consumption** | The system blocks **content consumption** (reading/watching) at the same time on **2 or more devices** of the same account — any combination of content types (e.g. reading a novel on device A while watching an anime on device B); applies to reading/watching activity (opening a chapter/episode counts as consuming), **not applicable** to AI features. |

### BR-5. Events

| Code   | Rule                            | Detail                                                                                                                                                                                                                                                                                      |
| ------ | ------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| BR-5.1 | **Event time window**           | Events take place within a specific time window created by the Operation Admin.                                                                                                                                                                                                             |
| BR-5.2 | **Unlock mechanism**            | Events support **3 unlock mechanisms**: **Free pass** (chapter/episode unlocked directly per event configuration), **Ad watch** (watch the ad to the end to unlock), **Time-till-free** (first chapter/episode unlocked for free, wait X minutes per configuration to unlock the next one). |
| BR-5.3 | **Unlocked content validity**   | Content unlocked from an event is accessible for **72 hours** from the moment of unlock (configurable default).                                                                                                                                                                             |
| BR-5.4 | **Bonus Coin from Coin Bundle** | Each term receives **1 separate Bonus Coin batch**, expiring after **30 days** from receipt (reference BR-1.2); coin deduction order follows BR-1.3 (batch nearest to expiry deducted first).                                                                                               |

### BR-6. Payments & Transaction Processing

| Code   | Rule                                 | Detail                                                                                                                                                                                                                                                                                                                                                                                                         |
| ------ | ------------------------------------ | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| BR-6.1 | **No-refund policy**                 | All digital transactions (Coin top-up, subscription purchase, chapter/episode purchase) are **non-refundable**, except for technical errors from the payment gateway. Chapters/episodes purchased with Coin are **permanently owned and non-refundable under all circumstances** — including when a title is archived (completely hidden; ownership is retained and restored when the title returns — BR-8.5). |
| BR-6.2 | **Interrupted transaction handling** | If the payment gateway confirms success but the wallet has not received the funds, the system **automatically reconciles and updates Coin/VIP within 24 hours**.                                                                                                                                                                                                                                               |

### BR-7. AI Features

| Code   | Rule                              | Detail                                                                                                                                                        |
| ------ | --------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| BR-7.1 | **Basic AI for registered users** | Basic AI features — **Search AI (semantic search)** and **Recommendation AI (content recommendation)** — are available **only to registered users (Reader)**. |
| BR-7.2 | **Advanced AI for subscribers**   | Advanced AI features — **Role Playing** and **Contextual Q&A** — are available only to users with an **active subscription** (Standard or Coin Bundle).       |
| BR-7.3 | **Guest cannot use AI**           | **Guest (not logged in) has no access to any AI feature**, including basic AI.                                                                                |
| BR-7.4 | **Revoke advanced AI on expiry**  | When a subscription expires, advanced AI access is revoked immediately; basic AI remains available to the Reader.                                             |

### BR-8. Title Lifecycle

| Code   | Rule                  | Detail                                                                                                                                                                                                                                       |
| ------ | --------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| BR-8.1 | **States**            | A title has 5 states: **draft, ongoing, paused, finished, archived**.                                                                                                                                                                        |
| BR-8.2 | **State transitions** | `draft` → ongoing/paused/finished/archived; `ongoing` ↔ `paused`; `ongoing`/`paused` → finished/archived; `finished` → archived (**cannot return** to a previous state); `archived` → ongoing/paused/finished (**cannot return to draft**).  |
| BR-8.3 | **Delete title**      | Only titles in the **draft** state can be deleted. A title that has left draft **cannot be deleted from the system** and **cannot return to draft**.                                                                                         |
| BR-8.4 | **Paused**            | The title temporarily **stops releasing new chapters/episodes**; all already-released content remains normally accessible.                                                                                                                   |
| BR-8.5 | **Archived**          | The title is **completely hidden** from users (catalog, search, detail page); permanently owned purchases made with Coin are retained and **fully restored when the title returns** to another state; spent coins are not refunded (BR-6.1). |

## 3. Users

| Role                | Description           | Primary permissions                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| ------------------- | --------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Guest**           | Not logged in         | – Browse the title catalog, view introductory information.<br>– Read/watch chapters/episodes marked Always Free per the title configuration.<br>– View the News/Announcement Board.<br>– Cannot comment, buy coin, subscribe, or use any AI feature.                                                                                                                                                                                                                        |
| **Reader** (User)   | Logged-in user        | – All Guest permissions (logged in).<br>– Purchase chapters/episodes with Coin, top up Coin, buy Subscription plans.<br>– Manage the Coin wallet (view history, balance, validity).<br>– Comment, star-rate, add to favorites.<br>– Use basic AI (Search AI, Recommendation AI); advanced AI (Q&A, Role Playing) only with an active subscription.<br>– Receive notifications via Email/Web/Mobile Push.                                                                    |
| **Content Manager** | Content management    | – Manage titles (add/edit, cover image, description, genres, age labels P/13+/16+/18+) and the **title lifecycle** per BR-8; can only delete titles in draft state.<br>– Publish new chapters/episodes, update content.<br>– Set release schedules.<br>– Configure the number of **Always Free** chapters/episodes (unlimited).<br>– Post to the News/Announcement Board of each title.<br>– **Does not have** the right to configure temporary free content for campaigns. |
| **Moderator**       | Community moderation  | – Handle violating comments (hide, delete, warn).<br>– Approve/reject Spoiler tags attached to comments.<br>– Manage user reports.<br>– Apply penalties (suspend commenting for 7 days after 3 spoiler violations).                                                                                                                                                                                                                                                         |
| **Operation Admin** | Operations & business | – Configure subscription plans (name, price, supported title list, Bonus Coin).<br>– Create/manage promotions and events (discounts, unlock mechanisms per BR-5.2).<br>– Mark **temporary free** chapters/episodes during a campaign.<br>– Select supported titles in each subscription plan.<br>– View revenue, traffic/views, and growth dashboards.<br>– **Does not have** the right to change the Content Manager's Always Free settings.                               |
| **System Admin**    | System administration | – Manage all user accounts (lock/unlock, reset password).<br>– Assign system access permissions.<br>– Configure technical parameters (device limit, coin expiry, security thresholds, etc.).<br>– Monitor infrastructure (server, storage, bandwidth, system logs).<br>– Manage system logs and data backups.                                                                                                                                                               |

## 4. Product

### 4.1. Components

DOKJA is a digital content distribution platform with 4 main components:

| Component                | Description                                                                    | Users                                                     |
| ------------------------ | ------------------------------------------------------------------------------ | --------------------------------------------------------- |
| **Web App**              | Responsive interface on the browser (desktop & mobile web)                     | Guest, Reader                                             |
| **Mobile App (Android)** | Android app for optimal viewing/reading experience, supports Mobile Push (FCM) | Guest, Reader                                             |
| **Admin Portal**         | Web-based admin interface dedicated to the operations team                     | Content Manager, Moderator, Operation Admin, System Admin |
| **API Server**           | Backend platform providing APIs for Web App, Mobile App, Admin Portal          | Internal system                                           |

### 4.2. Distribution Model

- **Direct Publishing:** DOKJA negotiates content licenses from partners and distributes directly to
  end users.
- **Freemium:** users can experience free content before deciding to pay.
- **Subscription:** the Standard and Coin Bundle plans provide access to a supported title list.
- **Coin-based:** purchase individual chapters/episodes with Coin, owning them permanently after
  purchase.

### 4.3. Design Principles

- Display language: **Vietnamese** (no multi-language support).
- Currency: **VND**.
- Payments: integrates **Sepay** (including the payment gateway and Sepay eInvoice e-invoicing).
- Age classification: all content must be labeled **P/13+/16+/18+** before release; the system
  blocks access to content unsuitable for the account's declared age; **Guests are blocked from all
  18+ content** (not shown in the catalog, search, or detail page).
- Content protection: original files are stored securely, video/image streaming is encrypted,
  preventing unauthorized downloads.

## 5. Requirements

### 5.1. Functional

#### 5.1.1. Common Platform

| Code        | Requirement                            | Detail                                                                                                                                                       | Reference |
| ----------- | -------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------ | --------- |
| F-COMMON-01 | Registration & login                   | Email/username + password; has a login session; supports logout, forgot password; optional remember-me (persistent login session).                           | –         |
| F-COMMON-02 | RBAC                                   | Each account belongs to one or more roles (Reader, Content Manager, Moderator, Operation Admin, System Admin); API/UI checks permissions before each action. | –         |
| F-COMMON-03 | Device management                      | Tracks the list of logged-in devices; limits to a maximum of 3 devices; allows remote logout of a device.                                                    | BR-4.1    |
| F-COMMON-04 | Block simultaneous content consumption | Blocks content consumption (reading/watching) on the same account on ≥2 devices at the same time — any combination of content types (novel, comic, anime).   | BR-4.2    |
| F-COMMON-05 | Age classification & display           | Collects date of birth at registration; each title displays a P/13+/16+/18+ label; hides/denies access to content unsuitable for the declared age.           | –         |

#### 5.1.2. Content Consumption

| Code         | Requirement                        | Detail                                                                                                                                                     | Reference      |
| ------------ | ---------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------- |
| F-CONTENT-01 | Anime Streaming                    | Watch high-quality video (HD/4K) via the built-in player; encrypted streaming; remembers watch position.                                                   | –              |
| F-CONTENT-02 | Comic Reader (Webtoon)             | Read Webtoons by vertical scroll, chapter by chapter; displays well on Web/Android; remembers reading position.                                            | –              |
| F-CONTENT-03 | Novel Reader                       | Minimal interface; customize font size, font, background color, line spacing; remembers reading position.                                                  | –              |
| F-CONTENT-04 | Freemium (Always Free)             | Displays Always Free chapters/episodes to the relevant audiences per configuration.                                                                        | BR-3.1, BR-3.3 |
| F-CONTENT-05 | Purchase chapter/episode with Coin | Purchase individual chapters/episodes with Coin; permanently unlocked after purchase.                                                                      | BR-1.3, BR-1.4 |
| F-CONTENT-06 | Content via subscription           | Unlimited access to chapters/episodes of titles in the plan while the subscription is active; on expiry, access is revoked.                                | BR-2.3, BR-2.4 |
| F-CONTENT-07 | Title information                  | Title detail page: name, author, description, genres, age label, number of chapters/episodes, release schedule, average star rating, chapter/episode list. | –              |

#### 5.1.3. Interaction & Personalization

| Code          | Requirement                 | Detail                                                                                                                     | Reference |
| ------------- | --------------------------- | -------------------------------------------------------------------------------------------------------------------------- | --------- |
| F-INTERACT-01 | Star Rating                 | Readers rate titles 1–5 stars; displays the average; each user rates once and can change it.                               | –         |
| F-INTERACT-02 | Favorite                    | Add/remove titles to/from a favorites list; displayed on the profile page.                                                 | –         |
| F-INTERACT-03 | Comments                    | Logged-in users comment by chapter/episode; sorted by time; paginated, aggregation in title detail.                        | –         |
| F-INTERACT-04 | Automatic comment filtering | Automatically hides/flags comments containing profanity, harassment, or spoilers (keywords + algorithm, configurable).     | –         |
| F-INTERACT-05 | Spoiler tag                 | Users tag "Spoiler" when writing a comment; spoiler comments are collapsed, showing a warning before expanding.            | –         |
| F-INTERACT-06 | Report violations           | Users report violating comments; Moderator handles them via the Admin Portal.                                              | –         |
| F-INTERACT-07 | Handle spoiler violations   | Counts spoiler violations per user; automatically locks commenting for 7 days at 3 violations; Moderator can unlock early. | –         |
| F-INTERACT-08 | Watch/read history          | Automatically saves watch/read history; displayed on the profile page; users can delete history.                           | –         |
| F-INTERACT-09 | Position bookmark           | Automatically saves the reading/watching position; syncs between Web and Android; resumes from the saved position.         | –         |

#### 5.1.4. Coin Wallet & Commerce

| Code        | Requirement                      | Detail                                                                                                                                                                | Reference              |
| ----------- | -------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------- |
| F-WALLET-01 | Coin Wallet                      | Each user has a Coin wallet: Paid/Bonus Coin balance, total coins; transaction history (top-up, spending, bonus received, expiry).                                    | –                      |
| F-WALLET-02 | Top up Coin via Sepay            | Select a Coin package (configured by Operation Admin), pay via Sepay; confirmation of success → add Paid Coin to the wallet.                                          | –                      |
| F-WALLET-03 | E-invoice                        | Always issues a personal e-invoice via Sepay eInvoice and sends it to the registered email for every successful Coin top-up (no tax ID required).                     | –                      |
| F-WALLET-04 | Bonus Coin expiry                | Automatically updates Bonus Coin expiry after 30 days; clearly displays the expiration time.                                                                          | BR-1.2                 |
| F-WALLET-05 | Subscription plans               | Operation Admin creates/configures plans (Standard, Coin Bundle) with terms configured by the Admin (no hardcoded limited list) including discounts; no auto-renewal. | BR-2.5, BR-2.6, BR-2.7 |
| F-WALLET-06 | Supported title list             | Each plan links a supported title list selected by the Operation Admin; updated in real-time when titles change.                                                      | BR-2.3, BR-2.5         |
| F-WALLET-07 | Activate subscription            | Successful payment via Sepay → activates the subscription immediately; e-invoice per F-WALLET-03.                                                                     | –                      |
| F-WALLET-08 | Subscription expiry              | Revokes subscription access on expiry; chapters purchased with Coin retain permanent rights.                                                                          | BR-2.4                 |
| F-WALLET-09 | Event                            | Operation Admin creates events: Coin/subscription discounts, free chapter unlocks per the BR-5.2 mechanism.                                                           | BR-5.1, BR-5.2         |
| F-WALLET-10 | Unlock content from events       | Chapters/episodes unlocked per the BR-5.2 mechanism are temporarily accessible for 72 hours.                                                                          | BR-5.2, BR-5.3         |
| F-WALLET-11 | Temporary free (campaign)        | Operation Admin marks chapters/episodes free during a campaign; when the campaign ends they automatically revert to paid status.                                      | BR-3.4                 |
| F-WALLET-12 | Interrupted transaction handling | Job automatically reconciles with Sepay; successful transactions where the wallet was not updated → add Coin/activate subscription within 24h.                        | BR-6.2                 |
| F-WALLET-13 | Transaction disputes             | Admin Portal displays a list of disputes; Operation Admin handles them within a maximum of 3 working days.                                                            | –                      |

#### 5.1.5. Artificial Intelligence (AI)

| Code    | Requirement                 | Detail                                                                                                                                                                                                                           | Reference |
| ------- | --------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------- |
| F-AI-01 | Search AI (Semantic Search) | Enter a natural-language query → the system understands the semantics and returns matching titles/chapters (third-party AI API); results include free and paid content (paid shows locked status), limited by the account's age. | BR-7.1    |
| F-AI-02 | Recommendation AI           | Recommends titles based on read/watch history, favorite genres, and similar user behavior; shown on the home page and detail pages.                                                                                              | BR-7.1    |
| F-AI-03 | Contextual Q&A              | Ask about a title's plot, characters, and relationships → AI answers based on that title's content context.                                                                                                                      | BR-7.2    |
| F-AI-04 | Role Playing                | Text-based conversational interaction with a story character (AI plays the character).                                                                                                                                           | BR-7.2    |
| F-AI-05 | API key & cost management   | Stores third-party API configuration (endpoint, API key, model, token limits); System Admin configures via the Admin Portal.                                                                                                     | –         |
| F-AI-06 | Abuse control               | Limits the number of AI calls per user/day; filters inappropriate content before sending to the AI.                                                                                                                              | –         |

#### 5.1.6. Administration & Reporting

| Code       | Requirement                    | Detail                                                                                                                   | Reference              |
| ---------- | ------------------------------ | ------------------------------------------------------------------------------------------------------------------------ | ---------------------- |
| F-ADMIN-01 | Revenue dashboard              | Revenue by day/week/month, by type (coin top-up, subscription, per-item purchase); growth charts.                        | –                      |
| F-ADMIN-02 | Traffic/view dashboard         | Views/reads, active users, views by title/device/access channel.                                                         | –                      |
| F-ADMIN-03 | Growth reports                 | Number of registered users, paying user conversion rate, retention rate.                                                 | –                      |
| F-ADMIN-04 | Titles & catalog management    | Content Manager manages titles: basic info, genres, age labels, cover image; manages the title lifecycle.                | BR-8                   |
| F-ADMIN-05 | Chapter/episode management     | Content Manager publishes chapters/episodes: upload content, release schedule, mark Always Free.                         | BR-2.3, BR-3.2         |
| F-ADMIN-06 | Release schedule               | Content Manager views/edits each title's release schedule; the system reminds when a deadline approaches.                | –                      |
| F-ADMIN-07 | News/Announcement Board        | Content Manager CRUD for a title's news board posts (schedule, delay, updates...); displayed publicly on the title page. | –                      |
| F-ADMIN-08 | Comment & report management    | Moderator handles violating comments and user reports (hide/delete/warn); saves handling history.                        | –                      |
| F-ADMIN-09 | Event management               | Operation Admin creates events: name, time, promotion type, unlock mechanism, applicable titles/chapters, budget.        | BR-5.1, BR-5.2         |
| F-ADMIN-10 | Plan management                | Operation Admin creates/edits/disables subscription and Coin plans (price, benefits, supported title list).              | BR-2.5                 |
| F-ADMIN-11 | Account management             | System Admin views all accounts, locks/unlocks, resets passwords, assigns internal roles.                                | –                      |
| F-ADMIN-12 | System parameter configuration | System Admin configures: max devices, Bonus Coin validity , event content validity , AI parameters.                      | BR-1.2, BR-4.1, BR-5.3 |
| F-ADMIN-13 | Infrastructure monitoring      | System Admin views server status, storage, bandwidth, error logs; alerts when thresholds are exceeded.                   | –                      |
| F-ADMIN-14 | Audit log                      | Logs important admin actions (login, content edits, configuration, violation handling) for audit purposes.               | –                      |

#### 5.1.7. Notifications & News Board

| Code       | Requirement              | Detail                                                                                                    | Reference |
| ---------- | ------------------------ | --------------------------------------------------------------------------------------------------------- | --------- |
| F-NOTIF-01 | New chapter notification | Auto-sent when a followed/read title releases a new chapter/episode (Email + Web Push + Mobile Push FCM). | –         |
| F-NOTIF-02 | Event notification       | Sent when there are new events, promotions, or subscription plan changes.                                 | –         |
| F-NOTIF-03 | Transaction notification | Sends Coin top-up and subscription/chapter purchase results (success/failure).                            | –         |
| F-NOTIF-04 | Notification center      | Notification list in the profile; unread/read status; mark as read.                                       | –         |
| F-NOTIF-05 | Notification settings    | Users toggle each notification type (new chapter, events, transactions).                                  | –         |

### 5.2. Non-functional

| Code   | Requirement                    | Detail                                                                                                                                                                                                                                 |
| ------ | ------------------------------ | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| NFR-01 | API performance                | Maximum API response time of **2 seconds** for 95% of requests (excluding background tasks).                                                                                                                                           |
| NFR-02 | Load capacity                  | The system supports a minimum of **1,000 concurrent users** in the initial phase.                                                                                                                                                      |
| NFR-03 | Data security                  | Encrypts sensitive data (passwords, payment info); protects original content files (encrypted streaming, prevents unauthorized downloads).                                                                                             |
| NFR-04 | Authentication & authorization | Uses secure JWT/session; RBAC checks permissions on both frontend and backend.                                                                                                                                                         |
| NFR-05 | Scalability                    | Modular architecture, separate API server, dedicated storage (CDN for large video/images); easy to scale nodes as users grow.                                                                                                          |
| NFR-06 | Device compatibility           | Web App supports popular browsers (Chrome, Safari, Firefox, Edge) and displays well on mobile; Mobile App supports Android 8.0 and above.                                                                                              |
| NFR-07 | AI limits                      | Each user is limited in the number of AI calls per day (default 50, configurable); limits input length to control third-party API cost (details at F-AI-06).                                                                           |
| NFR-08 | Data synchronization           | Read history/bookmarks sync between Web and Android with a maximum latency of 1 minute.                                                                                                                                                |
| NFR-09 | Observability                  | Unified logging, metrics, and tracing across API Server, Web App, Mobile App, and Admin Portal; structured logs with request IDs, key performance/error metrics, and alerting when thresholds are exceeded.                            |
| NFR-10 | Audit logging                  | All important actions (login/logout, content edits, configuration changes, violation handling, payment transactions) are recorded with actor, timestamp, and before/after values; logs are tamper-evident and retained per compliance. |
| NFR-11 | Loose coupling                 | Components are designed with loose coupling, modular and independently deployable, communicating only via well-defined interfaces.                                                                                                     |
| NFR-12 | Payment data privacy           | The system does not store user payment information (credit card number, CVV, ...) in the database; payment details are handled by the Sepay gateway only.                                                                              |

## 6. Out Of Scope

1. Copyright management is out of scope: contract negotiation, licensing, revenue sharing.
2. Partners have no accounts or portal access.
3. Vietnamese only. No multi-language support.
4. Android only. iOS is deferred.
5. No user-uploaded content. Only comments and ratings.
6. No live-streaming.
7. No audiobooks, podcasts, or music.
8. No chat. Notifications are one-way only.
9. Role Playing is text-only. No voice input or output.

## 7. Milestones

Milestones are divided by domain boundary, each phase delivering a deployable increment. The
priority order is based on core value: content management → content consumption → monetization →
community → AI → reporting.

There are some constraints:

- Observability must be implemented before any other feature.
- Management must be implemented before consumption.
- AI must be implemented in a separate phase.
- Mobile and reporting will be implemented in a later phase.
- The system must be fully functional by the end of the project.
