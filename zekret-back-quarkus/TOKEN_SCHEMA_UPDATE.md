# Token Schema Update

## Problem
The `tbl_token` table was using `TEXT` columns with `UNIQUE` constraints, which caused a MySQL error:
```
BLOB/TEXT column 'access_token' used in key specification without a key length
```

Additionally, tokens were being truncated because the initial `VARCHAR(512)` length was insufficient:
```
Data truncation: Data too long for column 'access_token' at row 1
```

## Solution

### 1. Code Changes
Modified `Token.java` entity to use `TEXT` columns without unique constraints:

**Before:**
```java
@Lob
@Column(name = "access_token", nullable = false, unique = true, columnDefinition="TEXT")
private String accessToken;

@Lob
@Column(name = "refresh_token", nullable = false, unique = true, columnDefinition="TEXT")
private String refreshToken;
```

**After:**
```java
@Lob
@Column(name = "access_token", nullable = false, columnDefinition = "TEXT")
private String accessToken;

@Lob
@Column(name = "refresh_token", nullable = false, columnDefinition = "TEXT")
private String refreshToken;
```

### 2. Database Schema Changes
Removed unique indexes that were preventing TEXT column modification:

```sql
-- View existing indexes
SHOW INDEX FROM tbl_token;

-- Remove unique constraints
ALTER TABLE tbl_token DROP INDEX UKaye8jnjoiuoy4h1pwaqkfenuq;
ALTER TABLE tbl_token DROP INDEX UKophdkgsdkbulp8ilue8a2j7fr;
```

### 3. Current Schema
After the changes, `tbl_token` has:
- PRIMARY KEY on `id`
- Foreign key index on `user_id`
- No unique constraints on `access_token` or `refresh_token`

## Important Notes

⚠️ **Token Uniqueness**: Since the unique constraints were removed from the database, you must validate token uniqueness in your application code before insertion to prevent duplicates.

### Recommended Implementation
```java
// Before saving a new token, check if it already exists
Token existingToken = Token.find("accessToken", newToken.getAccessToken()).firstResult();
if (existingToken != null) {
    // Handle duplicate token
    throw new TokenAlreadyExistsException();
}
```

## Alternative Solution (Optional)
If you want to maintain database-level uniqueness with a length limit, you can create partial unique indexes:

```sql
-- Create unique indexes with specified length (e.g., first 255 characters)
CREATE UNIQUE INDEX idx_access_token ON tbl_token (access_token(255));
CREATE UNIQUE INDEX idx_refresh_token ON tbl_token (refresh_token(255));
```

**Note**: This approach only guarantees uniqueness for the first 255 characters of the token.

## Commands Used

```bash
# Connect to MySQL container
docker exec -i bd mysql -uroot -proot zekretdb

# View indexes
SHOW INDEX FROM tbl_token;

# Drop unique indexes
ALTER TABLE tbl_token DROP INDEX UKaye8jnjoiuoy4h1pwaqkfenuq;
ALTER TABLE tbl_token DROP INDEX UKophdkgsdkbulp8ilue8a2j7fr;
```

## Date
December 3, 2025
