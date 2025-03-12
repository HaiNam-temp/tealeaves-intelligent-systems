![image](https://github.com/user-attachments/assets/dd23b0aa-bce6-48f0-9833-50597e5bf066)
# Smart Weighing Management System for Tea Cooperatives

## Introduction
The smart weighing management system helps tea cooperatives optimize the tea collection process from farmers. The system includes:
- **QR code scanning**: Confirms which farmer and cooperative the tea bag belongs to.
- **Smart scales**: Send tea bag weight data to the server.
- **Quantity tracking**: The collection company monitors the tea quantity on the server and only dispatches vehicles when the threshold is met, avoiding individual bag pickups.

## Key Features
1. **Send data from smart scales**
   - When farmers weigh tea at the warehouse, the system automatically sends the weight data to the server.
   
2. **Confirm tea bag information via QR code**
   - The QR code helps identify which farmer and cooperative the tea bag belongs to.
   
3. **Manage and track tea quantities**
   - Data is stored and updated continuously on the system.
   - The collection company can track the total amount of tea collected from cooperatives.
   
4. **Intelligent collection mechanism**
   - When the tea quantity at a cooperative reaches the collection threshold, the system notifies the collection company.
   - Reduces transportation costs and avoids individual bag pickups.

## Technologies Used
- **Backend**: Spring Boot (JPA, JWT, JavaMail)
- **Database**:MySQL
- **Frontend**: HTML, JavaScript,
- **Security**: Spring Security, JWT Authentication
- **IoT Communication**: MQTT or HTTP API
