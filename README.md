# EChestPlus

A lightweight Minecraft plugin that enhances the EnderChest system with upgrades and admin management.

---

## Features

- Expandable EnderChests with configurable sizes.
- Upgrader items that increase player chest capacity.
- Admin commands to manage player EnderChests (`/adminec`).
- Supports multiple storage backends: **SQLite**, **MySQL**, **MongoDB (future)**.

---

## Commands

### Admin
- `/adminec getItem <player>` – Give EnderChest upgrade to a player  
- `/adminec open <player>` – Open a player's EnderChest  
- `/adminec size <player> <lines>` – Change EnderChest size (1–6 lines)  
- `/adminec reload` – Reload plugin  
  Permission: `nightzy.echestplus.admin`

---

## Configuration

- **baseType** – Storage backend (`SQLITE`, `MYSQL`, `MONGODB`)  
- **databaseConnectionUri** – Full connection URI for database  
- **migrateStatus** – Enable migration of existing player EnderChests  
- **enderChestName** – GUI title of the EnderChest  
- **enderChestSize** – Default chest size (slots)  
- **upgraderItem** – Custom item to upgrade EnderChest  
- **adminMessages** – Customizable messages for admin commands

---

## Installation

1. Drop the `EChestPlus.jar` into your server `plugins` folder.
2. Start the server to generate the default `config.yml`.
3. Configure database and plugin options as needed.
4. Restart the server.

---

## Notes

- Maximum EnderChest size: **54 slots**.  
- Default upgrades add **9 slots per item**, max **+3 lines**.  
- Migration copies player chests safely; old items are removed after migration.  