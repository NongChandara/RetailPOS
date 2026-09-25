# Firebase Hosting Guide for TR Coffee (nong.chandara@gmail.com)

This project is pre-configured with `firebase.json` pointing to your `/public` folder containing `index.html`, `logo.png`, and `app-debug.apk`.

---

## Part 1: Initial Setup (One-Time)

### 1. Create a Firebase Project
1. Open the [Firebase Console](https://console.firebase.google.com/) with your Google Account (`nong.chandara@gmail.com`).
2. Click **"Add project"** (e.g. Project Name: `tr-coffee-cambodia`).
3. (Optional) Disable Google Analytics or leave it on, then click **"Create project"**.

### 2. Install Firebase CLI & Login on Your Computer
In your terminal (Mac / Windows PowerShell / Linux):
```bash
# Install Firebase CLI if not already installed
npm install -g firebase-tools

# Login with your Google account
firebase login
```

---

## Part 2: Connect & Deploy

### 1. Link Your Project
In this project's folder, run:
```bash
# Select your newly created Firebase project
firebase use --add
```
Choose your Firebase project from the list and give it an alias (like `default`).

### 2. Deploy Live
Run:
```bash
firebase deploy --only hosting
```

Your website will instantly go live with a free SSL certificate on:
- `https://<your-project-id>.web.app`
- `https://<your-project-id>.firebaseapp.com`

---

## Part 3: Connect Your Custom Domain

1. In the [Firebase Console](https://console.firebase.google.com/), go to **Build** -> **Hosting**.
2. Click **"Add custom domain"**.
3. Type your domain (e.g., `trcoffee.com` or `www.trcoffee.com`).
4. Firebase will display your DNS Verification records:
   - **TXT Record** (to prove domain ownership):
     - Host: `@`
     - Value: `google-site-verification=...`
   - **A Records** (IP addresses to point your website traffic):
     - Host: `@`
     - Value: `199.36.158.100` (Firebase provides 2 IP addresses)
5. Log into your domain registrar (e.g., Namecheap, Cloudflare, GoDaddy, or your local .com.kh registrar) and add these records in DNS management.
6. Firebase will automatically verify the records and provision a **free SSL certificate (HTTPS)** within 15–60 minutes.
