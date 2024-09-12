# LogCleaner

**LogCleaner** is a Spigot plugin that automatically cleans up Minecraft log files. It supports purging logs based on their age (in days) or by the number of logs to retain. The plugin is compatible with Minecraft versions 1.8 and above.

## Features

- Automatically deletes old `.log.gz` files in the `logs` folder.
- Option to purge logs based on their age (in days) or the number of logs to retain.
- The `latest.log` file is always preserved.
- Fully configurable via `config.yml`.

## Installation

1. Download the plugin and place the `.jar` file in your server's `plugins` folder.
2. Start your server to generate the `config.yml` file.
3. Adjust the settings in the `config.yml` to suit your needs.
4. Restart the server to apply the changes.

## Configuration

The `config.yml` looks like this:

```yaml
log-cleaning:
  mode: "days"  # "days" to delete based on age, "amount" to delete based on number of logs
  days: 3       # Number of days after which logs will be deleted if 'mode' is set to 'days'
  keep-logs: 3  # Number of .log.gz files to retain if 'mode' is set to 'amount'
