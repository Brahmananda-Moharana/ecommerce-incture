# Table of Contents

* [Project Overview](#project-overview)

  * [Key Capabilities](#key-capabilities)

* [Tech Stack](#tech-stack)

  * [Backend](#backend)
  * [Database](#database)
  * [Authentication](#authentication)
  * [Utilities](#utilities)
  * [Validation](#validation)
  * [Testing](#testing)
  * [Build Tool](#build-tool)

* [Project Architecture](#project-architecture)

  * [Package Structure](#package-structure)
  * [Controller Layer](#controller-layer)
  * [Service Layer](#service-layer)
  * [Repository Layer](#repository-layer)
  * [Entity Layer](#entity-layer)
  * [DTO Layer](#dto-layer)
  * [Security Layer](#security-layer)
  * [Request Flow Architecture](#request-flow-architecture)

* [Base URL](#base-url)

* [Authentication](#authentication)

* [API Access Control](#api-access-control)

  * [Public Endpoints](#public-endpoints-no-authentication-required)
  * [Admin Only Endpoints](#admin-only-endpoints)
  * [User Only Endpoints](#user-only-endpoints)
  * [Authenticated Endpoints](#authenticated-endpoints-user-or-admin)

* [User APIs](#user-apis)

  * [Register User](#register-user)
  * [Login User](#login-user)
  * [Get User By ID](#get-user-by-id)
  * [Update User](#update-user)
  * [Delete User](#delete-user-admin-only)

* [Product APIs](#product-apis)

  * [Create Product](#create-product-admin)
  * [Get All Products](#get-all-products)
  * [Get Product By ID](#get-product-by-id)
  * [Update Product](#update-product-admin)
  * [Delete Product](#delete-product-admin)

* [Cart APIs](#cart-apis)

  * [Add Product to Cart](#add-product-to-cart)
  * [Update Cart Item Quantity](#update-cart-item-quantity)
  * [Remove Item from Cart](#remove-item-from-cart)
  * [Get User Cart](#get-user-cart)

* [Order APIs](#order-apis)

  * [Checkout Order](#checkout-order)
  * [Get All Orders](#get-all-orders)
  * [Get Order By ID](#get-order-by-id)
  * [Update Order Status](#update-order-status-admin-only)

* [Roles](#roles)

* [Running the Project Locally](#running-the-project-locally)

  * [Prerequisites](#prerequisites)
  * [Clone the Repository](#clone-the-repository)
  * [Configure Database](#configure-database)
  * [Build the Project](#build-the-project)
  * [Run the Application](#run-the-application)
  * [Verify Application](#verify-application)
  * [Test APIs](#test-apis)

* [Project Structure](#project-structure-important-packages)

* [Default Application Port](#default-application-port)

* [Build Command Summary](#build-command-summary)

* [Database Schema](#database-schema)

* [Database Diagram](#database-diagram)
