# Balance Pay Macro

Fabric **1.21.11** client mod. **Needs only Fabric Loader. No Fabric API.**

No keybind. Once you load into a world/server it runs automatically: every 1 second it reads your *current* sidebar scoreboard balance and, **only if it is 500M or more**, runs `/pay <target> <amount>`. Pay-related chat messages, action bar text and titles are hidden so nothing pops up.

Edit the CONFIG block at the top of `src/main/java/com/balancepay/BalancePay.java`.

The jar is built by GitHub Actions: Actions tab -> latest run -> Artifacts -> balance-pay-macro.
