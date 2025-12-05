# MySQL 8.0 to MariaDB 11.2 Migration Guide

This document describes the steps taken to migrate the database from MySQL 8.0 to MariaDB 11.2 with memory optimization.

## Migration Steps

### 1. Update Docker Compose Configuration

Changed the database image from MySQL 8.0 to MariaDB 11.2 with optimized memory settings:

```yaml
services:
  bd:
    image: mariadb:11.2-jammy  # Changed from mysql:8.0
    command: >
      --max-connections=50
      --innodb-buffer-pool-size=128M
      --innodb-log-file-size=32M
      --innodb-flush-method=O_DIRECT
      --innodb-flush-log-at-trx-commit=2
      --query-cache-size=0
      --query-cache-type=0
      --performance-schema=OFF
      --skip-name-resolve
```

**Memory Optimization Benefits:**
- Reduced RAM usage from ~1-2GB to ~200-300MB
- `innodb-buffer-pool-size=128M`: Reduced from default ~1GB
- `performance-schema=OFF`: Saves ~400MB of RAM
- `max-connections=50`: Reduced from default 151

### 2. Convert SQL Dump Collation

MySQL 8.0 uses the `utf8mb4_0900_ai_ci` collation which is not available in MariaDB. The dump file needed to be converted:

**Backup original dump:**
```bash
cd dev/init
cp 00-Scripts.nogit.sql 00-Scripts.nogit.sql.mysql8.bak
```

**Replace incompatible collation:**
```bash
sed -i '' 's/utf8mb4_0900_ai_ci/utf8mb4_unicode_ci/g' 00-Scripts.nogit.sql
```

### 3. Clean Start with MariaDB

Remove old MySQL data and start fresh:

```bash
cd dev
rm -rf db-data
docker compose up -d
```

### 4. Verify Migration

Check that tables were created successfully:

```bash
docker exec -it bd mariadb -uroot -p -e "USE zekretdb; SHOW TABLES;"
```

## Key Differences Between MySQL 8.0 and MariaDB 11.2

| Feature | MySQL 8.0 | MariaDB 11.2 |
|---------|-----------|--------------|
| Default Collation | `utf8mb4_0900_ai_ci` | `utf8mb4_unicode_ci` |
| Memory Footprint | ~1-2GB | ~200-300MB (optimized) |
| CLI Tool | `mysql` | `mariadb` (or `mysql` for compatibility) |
| Health Check | `mysqladmin ping` | `healthcheck.sh --connect` |

## Connecting to MariaDB

Use any of these commands to connect:

```bash
# Using mariadb client
docker exec -it bd mariadb -uroot -p

# Using mysql client (compatibility)
docker exec -it bd mysql -uroot -p

# Connect directly to zekretdb database
docker exec -it bd mariadb -uroot -p zekretdb
```

## Troubleshooting

### Issue: Init scripts not running
**Solution:** Ensure the data directory is clean when first starting MariaDB. Init scripts only run on first initialization.

```bash
docker compose down
rm -rf db-data
docker compose up -d
```

### Issue: Collation errors
**Solution:** Verify all `utf8mb4_0900_ai_ci` references were replaced with `utf8mb4_unicode_ci` in the SQL dump.

```bash
grep -n "utf8mb4_0900_ai_ci" init/00-Scripts.nogit.sql
```

## Files Modified

- `docker-compose.yml`: Updated image and added memory optimization flags
- `init/00-Scripts.nogit.sql`: Converted collation for MariaDB compatibility
- `init/00-Scripts.nogit.sql.mysql8.bak`: Backup of original MySQL 8.0 dump
