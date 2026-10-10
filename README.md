# Balance Pay Macro

Fabric (1.21.4) client mod. Press **J** to toggle. Every 5s it reads your balance from the sidebar scoreboard (e.g. `$ 7.6K`) and runs `/pay <target> <amount>`.

Edit `TARGET`, `INTERVAL_SECONDS`, `MIN_AMOUNT` in `BalancePayClient.java`.

The jar is built by GitHub Actions: Actions tab -> latest run -> Artifacts -> balance-pay-macro.
