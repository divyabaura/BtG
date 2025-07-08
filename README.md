# 🔐 Break-the-Glass OBDA Mapping Toolkit

This toolkit provides a complete workflow for implementing **Break-the-Glass (BtG)** functionality in OBDA systems. It supports conditional access to sensitive data by combining policy-driven OBDA mappings with emergency access control based on user roles and justifications.

---

## 📌 Overview

In many sensitive domains like healthcare, emergency access to protected data is occasionally necessary. The **Break-the-Glass** approach enables selected users (e.g., doctors in emergencies) to override normal access restrictions temporarily—while still enforcing auditability and accountability.

This toolkit enables that by:

- Merging multiple OBDA mapping files (standard + BtG-specific)
- Applying role- and ID-based access filters only on BtG-sensitive mappings
- Enforcing runtime access control via a policy-aware query executor
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


---

## ⚙️ Workflow

### 1. PPOBDA Conversion (Optional)

If your original OBDA mappings need to be policy-aware, use the PPOBDA extension from the following repository:

🔗 [PPOBDA-with-Ontop](https://github.com/divyabaura/PPOBDA-with-Ontop)

This allows embedding access control directly into OBDA mappings based on JSON policy files.

---

### 2. BtG Mapping Combination

Run `BtGMappings.java` to combine:

- A full OBDA file (`all-policies.obda`)
- A BtG-specific OBDA file (`btg-policies.obda`)
- A set of authorized users and their roles

Only the BtG mappings are modified with user-role conditional filters:

```sql
WHERE (ontop_user('D_ID101') AND ontop_contains_role('EmergencyDoctors'))


