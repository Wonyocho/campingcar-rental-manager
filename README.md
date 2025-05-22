# Camping Car Rental Manager

A Java Swing GUI application backed by a MySQL relational database, for managing a camping‐car rental service with separate **Administrator** and **Member** modes.

---

## 📋 Project Description

This system allows:
- **Administrators** to initialize the database, perform full CRUD on all tables, view maintenance histories, and run arbitrary SQL queries.
- **Members** to browse available cars, make/cancel/modify reservations, and request external maintenance.
- A consistent GUI built with **Oracle JDK SE Swing** only (no external GUI libraries).
- Deployment as an **Eclipse** Java project (project folder name must be your student ID).

---

## ⚙️ Features

### Administrator
- **Database Initialization**  
  - Runs `202501-⟨학번⟩-ini.sql` to drop, recreate all tables and insert ≥12 sample rows each.
- **Full CRUD**  
  - Insert / Delete (by arbitrary condition) / Update on any table via GUI forms.
- **View All Tables**  
  - Grid view of every table’s current contents.
- **Maintenance Detail**  
  - Select a car → view its internal/external maintenance records → drill into parts or external workshop info.
- **Arbitrary SQL**  
  - Editor with syntax highlighting, supports ≥4-table JOIN, subqueries, GROUP BY. Includes three sample test queries.

### Member
- **Browse Cars**  
  - Filter by date, capacity, cost; view available dates.
- **Reservations**  
  - Create, cancel, change car or dates.
- **Maintenance Requests**  
  - Select external workshop & submit a request.
- **History & Export**  
  - View your rentals & maintenance → export to CSV/PDF.

---

## 🚀 Prerequisites

- **Java SE 17+** (Oracle JDK SE with Swing)
- **MySQL 8.0.15+**
- **Eclipse IDE** (Apple Silicon build on M1/M2 Macs)
- **Git** (for cloning)

1. **Clone the repo**  
   ```bash
   git clone https://github.com/YourUsername/campingcar-rental.git
   cd campingcar-rental
