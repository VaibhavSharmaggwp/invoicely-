# 📱 INVOICELY — Android Client (Jetpack Compose)

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-purple.svg?style=flat-square&logo=kotlin)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-Material_3-green.svg?style=flat-square&logo=android)](https://developer.android.com/jetpack/compose)
[![Retrofit](https://img.shields.io/badge/Retrofit-2.11-blue.svg?style=flat-square)](https://square.github.io/retrofit/)
[![Coroutines](https://img.shields.io/badge/Coroutines-StateFlow-red.svg?style=flat-square)](https://kotlinlang.org/docs/coroutines-overview.html)

**Invoicely Android** is a native, production-grade enterprise billing and financial mobile client built completely with **Jetpack Compose**, **Material 3**, and modern Android development best practices. It communicates directly with the Spring Boot microservice to provide merchants with instantaneous cash flow metrics, real-time ledgers, digital vouchers, and statutory corporate profile management.

---

## 🎨 Design Philosophy & Tokens

The app follows a bespoke high-contrast financial palette:
- **Ink Primary (`#151A11`)**: High-contrast, executive surface color used for cards, primary CTA buttons, and typography.
- **Chartreuse Neon (`#DCEF3C`)**: Vivid accent color highlighting critical financial totals, active badges, and growth pills.
- **Canvas (`#F6F5EC`)**: Warm, premium paper-like background reducing eye strain.
- **Muted Borders (`#E5E3D8`) & Surface (`#FFFFFF`)**: Clean structural borders giving components tactile depth.
- **Outfit & Monospace Typography**: Monospace formatting for currency amounts (`₹`) preventing visual jitter during value changes.

---

## 🏛️ Screens & Architecture

```
com.example.invoicely/
├── network/          # Retrofit API definitions, DTOs, and serialization models
├── security/         # EncryptedSharedPreferences TokenManager for JWT storage
├── state/            # Sealed UI state classes (Loading, Success, Empty, Error)
├── viewmodel/        # Architecture ViewModels with Coroutines and StateFlow
└── ui/theme/         # Composable screens, reusable design components, and navigation
```

### 1. 📊 Bento Financial Dashboard (`DashboardScreen.kt`)
- **Revenue Hero Card**: Displays real-time current month billed revenue, dynamic monthly growth percentage with contextual indicator (`▲`/`▼`).
- **Pair Metric Cards**: Live volume and count tracking:
  - **RECEIVED**: Live settled payments sum and count of cleared invoices.
  - **OUTSTANDING**: Total unpaid balance with dynamic due and late alerts.
- **Quick Action Strip**: One-tap shortcuts for New Invoice, Quick Share Link, and CSV Export.
- **Recent Invoices**: Real-time list of the 5 newest invoices with status tags (`ISSUED`, `PAID`, `PARTIALLY_PAID`, `OVERDUE`).

### 2. ⚡ Cross-Destination Reactive Synchronization (`AppNavigation.kt`)
- Employs Compose `savedStateHandle` (`refresh_dashboard = true`) between child destinations.
- Creating an invoice or confirming an offline payment automatically notifies the root backstack entry, re-fetching dashboard metrics seamlessly when popping back to the Home screen or switching tabs.

### 3. 🧾 Digital Paper Invoice Voucher (`InvoiceDetailScreen.kt`)
- **Perforated Canvas Layout**: Custom drawn dashed path divider and semicircular notches creating a tactile voucher aesthetic.
- **Auto-Injected Settlement Rail**: Automatically displays the merchant's Legal Entity Name, GSTIN, Account Number, IFSC, and UPI ID without manual data entry.
- **Record Payment Bottom Sheet**: Multi-method settlement dialog (`RAZORPAY`, `CASH`, `BANK_TRANSFER`, `UPI`, `CHEQUE`) supporting partial payments and automatic status upgrades.

### 4. 🏢 Company Details (`CompanyDetailsScreen.kt`)
- Form collecting merchant legal entity name, brand trade name, contact email, phone, registered address, and PIN code.
- **Strict GSTIN Validation**: Regex-enforced Indian GSTIN verification (`^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$`).

### 5. 💳 Payout & Settlement Rail (`BankAndUpiScreen.kt`)
- **Live Bank Preview Card**: Dynamic preview card reflecting bank name, account number, IFSC, and UPI ID in real time.
- **Account Number Confirmation**: Enforces dual account number entry matching to prevent typo errors.
- **RBI IFSC Validation**: Strict validation pattern (`^[A-Z]{4}0[A-Z0-9]{6}$`).

### 6. 📜 Unified Financial Ledger (`HistoryScreen.kt`)
- Live chronological stream combining invoice issuances, overdue events, and payment receipts.
- Filter chips (`ALL`, `SETTLED`, `ISSUED`, `OVERDUE`), search bar, and interactive bottom sheet receipts.

### 7. 🔐 Authentication & Security (`AuthScreen.kt`)
- Secure token storage using Android's `EncryptedSharedPreferences`.
- Real-time password strength analyzer with criterion checklists.
- Native Google OAuth 2.0 Credential Manager integration.

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio Ladybug (2024.2+)** or newer.
- **JDK 17+** or **JDK 21**.
- **Android SDK API 34+** (minSdk: 26, targetSdk: 34).

### Running the App
1. Open the `UI` directory in Android Studio.
2. Ensure your backend server is accessible (update base URL in `RetrofitClient.kt` if running on a physical device or emulator).
   - Emulator default: `http://10.0.2.2:8080/`
   - Local device: `http://<your-machine-ip>:8080/` or ngrok tunnel URL.
3. Build and launch:
   ```bash
   ./gradlew assembleDebug
   ```
