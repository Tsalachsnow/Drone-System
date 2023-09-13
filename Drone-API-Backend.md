

##DRONE SERVICE APPLICATION

##Introduction
This is the development of a major new Technology set to be a disruptive force in the field of transportation: **the drone**. 
It was Designed in accordance to the use case as was giving by the product owwner **Musala-Soft**
Although there might be some assumptions as was advised in that task description in **README.md** located in this project directory.

## APPLICATION DESIGN DOCUMENTATION

### Database Connection
For this Application an In-Memory database was Used (H2 database) as was advised in the task description. \
DRIVER CLASS: org.h2.Driver \
JDBC URL: jdbc:h2:mem:droneservicedb \
Username: sa \
Password: "" 

### This application consist of 2 main layers:
**1. The presentational layer**(Controllers) \
**2. The business layer**(Services)

The controllers are responsible for the communication with the client and the services are responsible for the business logic of the application.

The Services are divided into 2 main groups: \
**1. The Drone Services** \
**2. The Medication Services**

**The Drone Services** are responsible for the business logic of the drones while **The Medication Services** are responsible for the business logic of the medications.

**Furthermore,** The Drone Services are responsible for the management of the drones and the Medication Services are responsible for the management of the medications.

### The Drone Service Comprises of the following APIs:

****1. Drone Registration:**** \
****2. Fetch All Available Drones:**** \
****3. Fetch Battery Capacity:**** \
****4. Fetch Drone Status:**** \
****5. Dispatch Drone:**** \
****6. Charge Drone:**** \
****7. Periodic task to check drones battery levels every 40 seconds and history/audit event log was created for this.****

\
****XX. Drone Registration:**** 
This API is responsible for the registration of the drones. \
\
****Assumptions Made:**** \
**1.** On drone Registration validated the serial number is provided which is set as unique. \
**2.** On drone Registration the weight limit derived from the drone Model type. 

**Example:** \
Heavyweihgt drone has a weight limit of 500gr. \
Middleweight drone has a weight limit of 300gr. \
Lightweight drone has a weight limit of 200gr. \
Cruiserweight drone has a weight limit of 400gr. \
\
**3.** On drone Registration the battery status is set to Not-Charging. \
**4.** On drone Registration the drone state is set to IDLE. \
**5.** A Flag called batteryStatus was made available check for charging, not charging and fully charged of drone battery. \
**6.** On registration this flag is set NOT_CHARGING. 

Sample Payload: \
{ \
    "serialNumber": "ADF67890568", \
    "model": "Heavyweight", \
    "batteryCapacity": 95 \
} \
\
Sample Response: \
{ \
"responseCode": "000", \
"responseMessage": "Drone registered successfully", \
"serialNumber": "ADF67890568" \
} \
 \
The drone registration is done by sending a POST request to the following endpoint: \
http://localhost:8082/drone-api/registerDrone \
\
***XX. Fetch All Available Drones:***
This API is responsible for fetching all drones in their IDLE state. 
\
****Assumptions Made:**** \
**1.** On Fetching all available drones the drones are filtered by their state. \
\
Sample Response: \
{ \
"responseCode": "000", \
"drones": [ \
{ \
"serialNumber": "ADF67890568", \
"model": "Heavyweight", \
"weightLimit": 500, \
"weightLimitLeft": 500, \
"batteryCapacity": 95, \
"batteryStatus": "NOT_CHARGING", \
"state": "IDLE" \
}, \
{ \
"serialNumber": "ADF67890569", \
"model": "Cruiserweight", \
"weightLimit": 400, \
"weightLimitLeft": 400, \
"batteryCapacity": 50, \
"batteryStatus": "NOT_CHARGING", \
"state": "IDLE" \
} \
], \
"responseMessage": "List of Available Drones Fetched Successfully" \
} \
\
The drone registration is done by sending a GET request to the following endpoint: \
http://localhost:8082/drone-api/availableDrones 

***XX. Fetch Battery Capacity:*** This API is responsible for fetching the battery Status of a given drone at a time. \
\
****Assumptions Made:**** \
**1.** On Fetching the battery capacity the drone is filtered by its serial number. 

Sample Response: \
{ \
"responseCode": "000", \
"responseMessage": "Battery Capacity Fetched Successfully", \
"serialNumber": "ADF67890568", \
"batteryCapacity": 95, \
"batteryStatus": "NOT_CHARGING" \
} 

The drone registration is done by sending a GET request to the following endpoint: \
http://localhost:8082/drone-api/batteryCapacity/{serialNumber} 

***XX. Fetch Drone Status:*** This API is responsible for fetching the Status of a given drone at a time. 

****Assumptions Made:**** \
**1.** On Fetching the drone status the drone is filtered by its serial number. 

Sample Response: \
{ \
"responseCode": "000", \
"responseMessage": "Drone Status Fetched Successfully", \
"droneModel": "Heavyweight", \
"batteryCapacity": 95, \
"batteryStatus": "NOT_CHARGING", \
"droneCurrentState": "LOADED" \
} 

The drone registration is done by sending a GET request to the following endpoint: \
http://localhost:8082/drone-api/getDroneCurrentStatus/{serialNumber}

***XX. Dispatch Drone:*** This API is responsible for Dispatching drone for delivery after medications are being loaded successfully. 

****Assumptions Made:**** \
**1.** On Dispatch the drone is filtered by its serial number, the Drone State is set to DELIVERYING and the battery status is set to DISCHARGING. \
**2.** Dispatching of the drone is done by a Scheduler that executes every 10 seconds. \
**3.** The Scheduler also handles discharging of the battery level by 1% every 20 seconds of delivery until the expected delivery time is reached. \
**4.** Once the Expected time of delivery is reached the Scheduler is terminated, Drone State set to DELIVERED and Battery status is set to NOT_CHARGING. \
**5.** For Every Dispatch, the dispatch number must be unique. 

Sample Request payload: \
{ \
"serialNumber": "ADF67890568", \
"dispatchNumber":"5600", \
"expectedArrivalTime": "22:57:10" \
} 

Sample Response: \
{ \
"responseCode": "000", \
"responseMessage": "Drone with serial number ADF67890568 dispatched successfully", \
"currentState": "DELIVERING", \
"expectedDeliveryTime": "22:57:10" \
} 

The drone registration is done by sending a POST request to the following endpoint: \
http://localhost:8082/drone-api/dispatchDrone

***XX. Charge Drone:*** This API is responsible for Charging the Drone until it is Fully Charged. 

****Assumptions Made:**** \
**1.** On Charging the drone is filtered by its serial number and the battery status is set to CHARGING. \
**2.** Charging of the drone is done by a Scheduler that executes every 3 seconds. \
**3.** The Scheduler also handles charging of the battery level by 1% every 3 seconds until the battery capacity is 100. \
**4.** Once the battery capacity is 100 the Scheduler is terminated and the battery status is set to FULLY_CHARGED.

Sample Request payload: \
{ \
"serialNumber": "ADF67890565" \
} 

Sample Response: \
{ \
"responseCode": "000", \
"responseMessage": "SUCCESS",\
"serialNumber": "ADF67890565", \
"status": "CHARGING" \
} 

The drone registration is done by sending a POST request to the following endpoint: \
http://localhost:8082/drone-api/chargeBattery

### The Medication Service Comprises of the following APIs:

****1. Load Medication:**** \
****2. Fetch Loaded Medication:**** \
****3. Fetch Delivery Status:**** \
****4. Confirmation Of Delivery:****

\
***XX. Load Medication:*** This API is responsible for Loading a Drone with a List of medication until the weight limit for the drone is exceeded. \

****Assumptions Made:**** \
**1.** On Loading the drone is filtered by its serial number and the drone state is set to LOADING. \
**2.** Once it is set to loading, performing loading on this drone is not possible. \
**3.** After Loading the Drone State is set to LOADED Which still prevents this drone from being loaded again until The state is returned to IDLE. \
**4.** The drone can only be loaded again only if the weight-limit has not being exceeded. 

Sample Request payload: \
{ \
"serialNumber": "ADF67890569", \
"medications": [ \
{ \
"name": "paracetamol", \
"weight": 100, \
"code": "WS_234578", \
"image": "34567865JK" \
}, \
{ \
"name": "pain-killers", \
"weight": 100, \
"code": "WS_2345JL", \
"image": "34567865DY" \
}, \
{ \
"name": "cough-syrup", \
"weight": 200, \
"code": "WS_2345JK", \
"image": "34567865BN" \
} \
] \
} 

Sample Response: \
{ \
"responseCode": "000", \
"responseMessage": "medication loaded successfully", \
"loaded_drones_serial_number": "ADF67890569" \
}

The drone registration is done by sending a POST request to the following endpoint: \
http://localhost:8082/drone-api/loadMedication

***XX. Fetch Loaded Medication:*** This API is responsible for Fetching the List of Medications that are loaded on a given drone. 

****Assumptions Made:**** \
**1.** On Fetching the loaded medication the Medication table in the db is filtered by drone's serial number. 

Sample Response: \
{ \
"responseCode": "000", \
"responseMessage": "medications loaded on this drone", \
"medicationList": [ \
{ \
"name": "paracetamol", \
"weight": 100, \
"code": "WS_234578", \
"image": "34567865JK", \
"status": "LOADED" \
}, \
{ \
"name": "pain-killers", \
"weight": 100, \
"code": "WS_2345JL", \
"image": "34567865DY", \
"status": "LOADED" \
}, \
{ \
"name": "cough-syrup", \
"weight": 300, \
"code": "WS_2345JK", \
"image": "34567865BN", \
"status": "LOADED" \
} \
] \
}

The drone registration is done by sending a GET request to the following endpoint: \
http://localhost:8082/drone-api/getLoadedMedication/{serialNumber}

***XX. Fetch Delivery Status:*** This API is responsible for Fetching the Delivery Status of a given dispatched Medication. 

****Assumptions Made:**** \
**1.** On Fetching the delivery status the Dispatch-Log table in the db is filtered by dispatch number. \
**2.** The Delivery Status is determined by Confirmation of Delivery. \
**3.** The Delivery Status is set to DELIVERED once the confirmation of delivery is received. 

Sample Response: \
Before confirmation of Delivery: \
{ \
"responseCode": "000", \
"responseMessage": "Medications Dispatched", \
"expectedDeliveryTime": "15:51:10", \
"deliveryStatus": "DELIVERING" \
}

Sample Response: \
After confirmation of Delivery: \
{ \
"responseCode": "000", \
"responseMessage": "Medications Delivered Successfully", \
"expectedDeliveryTime": "15:51:10", \
"deliveryStatus": "DELIVERED" \
}

The drone registration is done by sending a GET request to the following endpoint: \
http://localhost:8082/drone-api/getDeliveryStatus/{dispatchNumber}

***XX. Confirmation Of Delivery:*** This API is responsible for Confirming that the dispatched items had being receivedS successfully. 

****Assumptions Made:**** \
**1.** On Confirmation of Delivery the Dispatch-Log table in the db is filtered by dispatch number. \
**2.** On Confirmation the Time of delivery is requested for who is confirming this delivery. \
**3.** The Delivery Status is set to DELIVERED once the confirmation of delivery is received. \
**4.** After confirmation the drone state is set to RETURNING as the drone begins a journey back home for a certain period of time which is estimated difference of the expected time of delivery and the actual time of delivery as stipulated by the receiver of this item. \
**4.** A Scheduler is used to handle the battery reduction by 1% every 20 seconds of its journey until the expected return time is reached. \
**5.** The drone state is set to IDLE once it returns home. 

Sample Request payload: \
{ \
"serialNumber": "ADF67890569", \
"dispatchNumber": "5600", \
"timeOfDelivery": "16:18:10" \
}

Sample Response: \
{ \
"responseCode": "000", \
"responseMessage": "Items Received, request for drone return with serial number :: ADF67890569 initiated", \
"serialNumber": "ADF67890569", \
"expectedReturnTime": "16:18:36", \
"receivedMedications": [ \
{ \
"name": "paracetamol", \
"weight": 100, \
"code": "WS_234578", \
"image": "34567865JK" \
}, \
{ \
"name": "pain-killers", \
"weight": 100, \
"code": "WS_2345JL", \
"image": "34567865DY" \
}, \
{ \
"name": "cough-syrup", \
"weight": 300, \
"code": "WS_2345JK", \
"image": "34567865BN" \
} \
]
}

The drone registration is done by sending a POST request to the following endpoint: \
http://localhost:8082/drone-api/confirmDelivery

