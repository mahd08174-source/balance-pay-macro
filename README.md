# Balance Pay Macro

Fabric **1.21.11** client mod. **Needs only Fabric Loader. No Fabric API.**

No keybind. Once you load into a world/server it runs automatically: every 1 second it reads your *current* sidebar scoreboard balance (any amount: `$350`, `$7,600`, `$7.6K`, `$1.2M`, `$2.5B`) and runs `/pay <target> <amount>`. The server's pay reply is hidden from chat.

Edit the CONFIG block at the top of `src/main/java/com/balancepay/BalancePay.java`.

The jar is built by GitHub Actions: Actions tab -> latest run -> Artifacts -> balance-pay-macro.
