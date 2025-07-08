# 🔐 Break-the-Glass OBDA Mapping Toolkit

This toolkit provides a complete workflow for implementing **Break-the-Glass (BtG)** functionality in OBDA systems. It supports conditional access to sensitive data by combining policy-driven OBDA mappings with emergency access control based on user roles and justifications.

---

## 📌 Overview

In many sensitive domains like healthcare, emergency access to protected data is occasionally necessary. The **Break-the-Glass** approach enables selected users (e.g., doctors in emergencies) to override normal access restrictions temporarily—while still enforcing auditability and accountability.

This toolkit enables that by:

- Merging multiple OBDA mapping files (standard + BtG-specific)
- Applying role- and ID-based access filters only on BtG-sensitive mappings
- Enforcing runtime access control via a policy-protected query executor
- Supporting both full and partial policies using JSON policy files

---

## 🧱 Repository Structure
├── BtGMappings.java # Java tool to merge OBDA mappings with role-based filters
├── BtGExecutor.java # Simulated executor that applies runtime access control
├── PolicyFile.json # Full policy set (includes all mappings)
├── BtGPolicy.json # Subset for Break-the-Glass (emergency access only)
├── BtGmappings.obda # Output: OBDA file with conditional mappings
├── example/
│ ├── all-policies.obda # OBDA with full mapping definitions
│ ├── btg-policies.obda # OBDA with BtG-restricted mappings
│ └── policies.md # Optional description of policies



## 🔄 Workflow: Break-the-Glass (BtG) Mapping Integration

This workflow explains how to generate and execute OBDA mappings with embedded **Break-the-Glass (BtG)** access control.

---

### 1️⃣ Prepare Policy-Embedded OBDA Mappings

Use the PPOBDA framework to generate OBDA mappings with embedded access policies:

🔗 Refer to: [https://github.com/divyabaura/PPOBDA-with-Ontop](https://github.com/divyabaura/PPOBDA-with-Ontop)

You need to generate two sets of mappings:

- `all-policies.obda` → Mappings with **all** access control policies (using `PolicyFile.json`)
- `btg-policies.obda` → Mappings with **only BtG** policies (using `BtGPolicy.json`)

These mappings reflect different access levels:
- Regular mappings enforce full policy constraints.
- BtG mappings contain rules for emergency override access.

---

### 2️⃣ Combine Mappings with User-Based BtG Conditions

Use the `BtGMapping.java` utility provided in this repository to combine the two mappings.

**Inputs:**

- `all-policies.obda` – Original, complete mapping
- `btg-policies.obda` – Break-the-Glass subset
- List of users and roles (e.g., `D_ID101` to `D_ID1200` with role `EmergencyDoctors`)

**Behavior:**

- The tool embeds conditional clauses **only into BtG mappings**
- These clauses enforce BtG access based on:
  - User ID
  - Role
  - Presence of justification

✅ Regular mappings remain untouched  
✅ BtG mappings gain `WHERE` clauses such as:

```sql
WHERE ontop_user('D_ID101') AND ontop_contains_role('EmergencyDoctors')
```

**Output:**
- `BtGmappings.obda` – Final mapping file with conditional emergency access logic

---

### 3️⃣ Execute Queries Under BtG Conditions

Use `BtGExecutor.java` to simulate query execution under BtG scenarios.

**Functionality:**

- Checks if the user is eligible for BtG access:
  - User ID is in allowed range (e.g., `D_ID101–D_ID1200`)
  - User role matches (`EmergencyDoctors`)
  - Justification is provided
- If BtG access is valid, it uses `BtGmappings.obda` to answer queries
- Otherwise, defaults to restricted `all-policies.obda`

This ensures sensitive data is accessed only under tightly controlled and auditable emergency conditions.

---

✅ This workflow mimics how emergency access would be safely controlled in real-world systems like healthcare databases.




