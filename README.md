Wallet Backend System

A scalable and distributed wallet backend system designed to handle user accounts and financial transactions efficiently. The system is built with a focus on scalability, 
consistency, and fault tolerance using modern distributed system design patterns.

Features
🔹 Scalable Wallet System
Designed and developed a robust backend system for managing user wallets and financial transactions.
Supports high-throughput transaction processing with reliability.

🔹 Horizontal Database Sharding
Implemented horizontal sharding using Apache ShardingSphere.
Used modulo-based routing to distribute data across multiple MySQL instances.
Ensures improved performance and scalability as data grows.

🔹 Distributed Transaction Management (Saga Pattern)
Built a Saga Orchestrator-based distributed transaction system.
Ensures data consistency across services using:
Compensating transactions
Failure recovery mechanisms
Improves fault tolerance in distributed environments.

🔹 Unique ID Generation (Snowflake Algorithm)
Developed a distributed unique ID generator using the Snowflake algorithm.
Guarantees:
Globally unique IDs
No collision across shards
Time-ordered identifiers

🔹 RESTful APIs
Designed and exposed REST APIs for:
Wallet creation
Credit/Debit operations
Transaction management
Ensures:
High availability
Scalability
Efficient request handling



Architecture Highlights
Distributed system design
Database sharding for scalability
Saga pattern for eventual consistency
Stateless REST services
Fault-tolerant transaction handling



Tech Stack
Java / Spring Boot
MySQL
Apache ShardingSphere
REST APIs
Distributed System Design Patterns


Key Outcomes
Improved system scalability with sharded databases
Ensured reliable distributed transactions
Eliminated ID collision using Snowflake
Built a production-ready backend system architecture
