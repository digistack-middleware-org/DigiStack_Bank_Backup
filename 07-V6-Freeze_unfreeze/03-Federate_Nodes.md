# Freeze/Unfreeze Explained in Simple English

## 1. What is Freeze/Unfreeze?

Think of your bank card.

- **Freeze** = you lock the card. Nobody can take money out or put money in.
- **Unfreeze** = you unlock it. Everything works again.

Banks do this in real life. Lost your card? You freeze it in the app. Found it? You unfreeze it.

---

## 2. Why do we need it?

- Protect money if an account is risky.
- Stop fraud quickly.
- Customer can pause their own account.

**Rule:** A frozen account must **reject** deposits and withdrawals.

---

## 3. Where is the Freeze code?

Good news: it was already written. We just need to **test it now**.

| Part | Where it lives |
|---|---|
| Freeze logic | `FreezeService.java` |
| Database freeze | `AccountDao.freeze()` / `unfreeze()` |
| Buttons on screen | `Freeze.jsp` / `Unfreeze.jsp` |
| Blocking deposits | `DepositService` checks `isFrozen()` |
| Blocking withdrawals | `WithdrawService` checks `isFrozen()` |

The database has a column called `is_frozen`.

- `is_frozen = false` → account works
- `is_frozen = true` → account is locked

---

## 4. Big idea: Never trust the screen alone

This is called **defense in depth**. Example:

- The website **greys out** the Deposit button when frozen.
- But a clever user can press F12 in the browser and enable the button anyway. Or use a tool like `curl` to send a request directly.

So the check must exist in **two places**:

1. **UI check** — hides and disables buttons (friendly, for normal users).
2. **Server check** — the backend refuses the money move even if someone bypasses the UI (real protection).

Real-life example: A shop door says "Closed" (UI), but the door is also **locked** (server). The sign alone doesn't stop anyone.

---

# All the Code will already Developed dont worry

## 10. One-line summary

> Sprint 3 is not about writing new code — it's about **proving** that freezing an account really blocks money movement, at the screen level AND at the server level, and that unfreezing brings the account back to life.

When all checks pass, say **"continue sprint"** to move to Sprint 4.
