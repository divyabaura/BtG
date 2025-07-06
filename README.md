# 🔐 Break-the-Glass OBDA Mapping Tool

This tool generates **Break-the-Glass (BtG)** OBDA mapping files based on user role and justification input. It is designed for environments that require controlled emergency access to sensitive data, such as healthcare or secure enterprise systems.

---

## 📌 Overview

In emergency scenarios, select users can temporarily override normal access restrictions using BtG policies. This tool merges:

- ✅ A full OBDA mapping file with standard policies
- ✅ A BtG-specific OBDA mapping file with sensitive policies
- ✅ A list of authorized users and their roles

It outputs a new mapping file (`BtGmappings.obda`) that includes conditional source clauses to enforce role-based access at the mapping level.

---

## 📁 Repository Contents

```bash
├── BtGMappings.java             # Java utility for mapping generation
├── example/
│   ├── all-policies.obda        # Complete OBDA mapping
│   ├── btg-policies.obda        # Subset with BtG-only mappings
│   └── policies.md              # Descriptions of included policies
