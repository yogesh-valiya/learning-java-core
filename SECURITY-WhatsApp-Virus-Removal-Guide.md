# 🛡️ How to Remove the "Estatement" WhatsApp Virus — A Simple Step‑by‑Step Guide

> **Who this guide is for:** anyone who received a file (often named something like
> **`Estatement.vbs`**) on **WhatsApp** and accidentally opened it. No technical
> knowledge needed — just follow the steps in order.

---

## ⚠️ First, the 30‑second summary

- The file you opened was **not** a bank statement. It was a **disguised virus** whose
  job is to **secretly hand control of your computer to a stranger over the internet**.
- It does this by quietly installing a real "remote control" program and pointing it at
  the attacker's server, so they can watch your screen, open your files, type commands,
  and steal passwords — **without you seeing anything**.
- **Windows Defender may not catch it**, because the tool it installs is technically
  legitimate software being misused. So you need to remove it yourself.
- **The most important first action: disconnect your computer from the internet right now.**

You have not done anything shameful — these scams are designed to fool careful people.
The rest of this guide walks you through cleaning it up.

---

## 🚨 Step 1 — Cut off the internet (do this first)

This stops the attacker from reaching your computer while you clean it.

- **Wi‑Fi:** click the Wi‑Fi icon at the bottom‑right of your screen and turn Wi‑Fi **off**,
  or click **"Disconnect."**
- **Cable (Ethernet):** simply unplug the internet cable from the computer.
- If you're not sure, turning on **Airplane mode** (bottom‑right menu) also works.

✅ **You'll know it worked** when the Wi‑Fi/network icon shows "not connected."

---

## 🤔 Step 2 — Answer one important question

Think back to the moment you opened the file. **Did a blue pop‑up appear asking
something like *"Do you want to allow this app to make changes to your device?"* —
and did you click *Yes*?**

- **You clicked "Yes"** → the virus most likely fully installed. Follow **all** the steps
  below, and please read **Step 8 (Extra protection)** carefully.
- **You clicked "No", or never saw such a pop‑up** → the virus probably **failed** to
  install. Still follow the steps below to be safe, but your risk is much lower.

Either way, continue with the cleanup below. 👇

---

## 🧹 Step 3 — Run the automatic cleanup tool

Your helper has prepared a cleanup script called **`Remove-EndpointCentralImplant.ps1`**.
It finds and removes the virus's pieces automatically. Here's how to run it safely.

1. **Save the file** somewhere easy to find, like your **Desktop**.
2. **Right‑click** the file `Remove-EndpointCentralImplant.ps1`.
3. Choose **"Run with PowerShell."**
4. A blue pop‑up will appear asking for permission (**"Do you want to allow…"**).
   This time it's **safe** — click **Yes**. (This is *your* cleanup tool, not the virus.)
5. A black window will open and text will scroll by. **Let it finish** — it may take
   several minutes, especially the virus scan near the end. Don't close it.
6. When it's done, it will print a short summary and save a **log file on your Desktop**
   (named `EndpointCentral-Removal_...log`). Keep that file in case you need help later.

> 💡 **If "Run with PowerShell" doesn't appear** when you right‑click, do this instead:
> click the **Start** button, type **`PowerShell`**, **right‑click** it, choose
> **"Run as administrator"**, then drag the script file into the window and press **Enter**.

### What the cleanup tool does for you (in plain words)
- 🚧 Blocks the attacker's server so it can't reach your PC.
- 🛑 Shuts down and deletes the hidden "remote control" program.
- 🗑️ Deletes the leftover virus files and hidden folders.
- 📜 Removes the fake "trust certificates" the virus added.
- 🔁 Removes the tricks that made the virus restart every time you turn on the PC.
- 🔍 Runs a full Windows Defender scan.
- ✅ Shows you a summary of whether anything is left.

---

## 🔎 Step 4 — Read the tool's final summary

At the end, the black window shows a few lines like:

```
Services remaining : 0
Processes remaining: 0
C2 connections     : 0
Rogue certs left   : 0
[RESULT] No implant artifacts detected after cleanup.
```

- **All zeros + green "No implant artifacts"** → great, the automatic cleanup succeeded. 🎉
- **Any number above 0, or a yellow warning** → some pieces remain. Don't panic — go to
  **Step 7 (When to get extra help)**.

---

## 🔐 Step 5 — Change your important passwords (from a *different* device)

**Important:** use your **phone or another clean computer** for this — *not* the infected
PC — until you're confident it's clean.

Change the passwords for, at minimum:

- 📧 Your **email** (do this first — it's the key to everything else)
- 🏦 **Banking / UPI / payment apps**
- 💬 **WhatsApp** and social media
- 🛒 Anything with **saved cards** (shopping, etc.)

And turn on **Two‑Factor Authentication (2FA / OTP)** wherever it's offered. It adds a
second lock so a stolen password alone isn't enough.

> 🏦 If you did any **banking** on the infected computer, **call your bank** and tell them
> your device may have been compromised. Watch for unexpected transactions.

---

## 📵 Step 6 — Warn your contacts and clean up WhatsApp

- These viruses often **message your contacts automatically** to spread further.
- Send a quick note to family/friends: *"If you got a file from me on WhatsApp, please
  don't open it — my account may have been misused."*
- **Delete the original file** from your WhatsApp chats and your Downloads folder.
- **Never open** files ending in **`.vbs`, `.exe`, `.scr`, `.bat`, `.js`** sent over chat,
  even from people you know. Real bank statements come as **PDFs** from official channels.

---

## 🆘 Step 7 — When to get extra help

Please ask a trusted technician (or your company's IT team) for help if **any** of these are true:

- The tool's summary showed **numbers above 0** or a **yellow warning**.
- You clicked **"Yes"** on the pop‑up in Step 2 (higher chance of deep infection).
- This computer is a **work computer** or connected to an **office network** — the virus
  can try to spread to other machines, so IT should be told **immediately**.
- You're simply **not comfortable** doing these steps alone. That's completely okay.

**Show them the log file** from your Desktop and the technical details in the
"For your technician" section at the bottom of this guide.

---

## 🛡️ Step 8 — The safest option (recommended if you clicked "Yes")

If the virus had full access (you approved the pop‑up), the **only 100% certain** way to
be clean is to **reinstall Windows** ("reset this PC" / a fresh install). A cleanup tool
removes the known pieces, but a program that had full control *could* leave changes behind.

- **How:** Settings → System → Recovery → **Reset this PC** → **Remove everything**.
- **Before doing this:** back up your personal files (photos, documents) to an external
  drive or cloud — but **do not** back up any programs or `.exe`/`.vbs` files.
- If unsure, this is a good moment to involve a technician (Step 7).

---

## ✅ Quick Checklist (print or screenshot this)

- [ ] Disconnected from the internet
- [ ] Ran `Remove-EndpointCentralImplant.ps1` as administrator
- [ ] Checked the tool's final summary (all zeros?)
- [ ] Changed email, banking, and WhatsApp passwords **from another device**
- [ ] Turned on Two‑Factor Authentication (2FA)
- [ ] Called the bank (if banking was used on this PC)
- [ ] Warned my contacts / deleted the original file
- [ ] Considered reinstalling Windows if I clicked "Yes"

---

## ❓ Frequently Asked Questions

**Q: My antivirus said everything is fine. Am I safe?**
Not necessarily. This virus installs a *legitimate* remote‑control program that many
antivirus tools don't flag. Trust the cleanup steps, not just the antivirus.

**Q: Can the virus steal what I already typed before I disconnected?**
Possibly, which is exactly why **changing passwords (Step 5)** matters. Assume anything
typed on the PC could have been seen, and reset those accounts.

**Q: Is my phone infected too?**
This particular virus targets **Windows computers**, not phones. Your phone is a safe
device to use for changing passwords.

**Q: I only received the file but never opened it. Do I need all this?**
No. If you never opened/ran it, just **delete the file** and don't open it. You're fine.

**Q: The black window showed some red or yellow text. Did it break something?**
The tool is designed to **skip past errors** and keep going, so occasional warnings are
normal. What matters is the **final summary** (Step 4).

---

## 🧑‍💻 For your technician / IT team (technical details)

Share this section with whoever helps you.

- **Threat type:** Malicious use of a legitimate RMM — **ManageEngine Endpoint Central
  (UEMS / Desktop Central) remote agent** silently installed and pointed at an
  attacker‑controlled server. Delivered by an obfuscated VBScript downloader
  (`Estatement.vbs`) received via WhatsApp.
- **Behaviour:** Stage‑1 `.vbs` (Base64 + reversed + XOR obfuscation) downloads a ~33 MB
  ZIP, extracts it, and runs `setup1.vbs`, which requests UAC elevation and silently
  installs the agent via `msiexec … /qn`, adds rogue root CAs, then self‑deletes the
  dropper folder. `RemCom.exe` (PsExec clone) enables spread to other domain machines.

**Indicators of Compromise (IOCs):**

| Type | Value |
|------|-------|
| C2 / management server | `134.122.133.157` (ports `8383`) |
| Payload URL / host | `https://zcyz.ccwu.cc/sys/G/1/c2b8.zip` — host `zcyz.ccwu.cc` |
| Server hostname | `WIN-EJ08Q1HNSV4` |
| Remote‑office auth key | `86eb482fe98959e9698b77ab575fd255` (branch/RO ID `301`) |
| Rogue root CAs | `DMRootCA`, `DMRootCA-Server` (issuer `WIN-EJ08Q1HNSV4`) |
| Drop folder | `C:\Users\Public\Documents\MSUpdate_#####\` |
| ZIP SHA‑256 | `519cb037bea6bd413736b789c4f618f3316f9ce71b057e5130147c7a2fca28ea` |
| `RemCom.exe` SHA‑256 | `7a1dcf22a67c194367d4ef65304cf69c11df0b14c3d08215b7ee81373a3cefba` |
| `UEMSAgent.msi` SHA‑256 | `92f68fc7f6d0f544a750bf67b4903d1a4062d1372b7f98ab5c2f698eb61359bb` |
| `dcremagentinstaller.exe` SHA‑256 | `927837f0b31d20c0ed7948cf6170b57b28bfd4bd118aa21853611062ed2678b2` |
| `UEMSAgent.mst` SHA‑256 | `180195558475b32abedb88b3103c06ee464148809986b78de32d8fdba897c516` |

**Remediation script:** `Remove-EndpointCentralImplant.ps1` (self‑elevating; blocks C2,
removes services/processes/install folders, uninstalls the MSI, removes rogue CAs, clears
persistence, runs Defender, prints verification). For confirmed elevated execution, prefer
a full OS reimage. If the host is domain‑joined, treat as a network incident and check
lateral movement via `RemCom.exe`.

---

*This guide was generated as part of a security analysis of a malware sample. It describes
defensive cleanup steps only. When in doubt, consult a qualified security professional.*
