# Banking Rest API

- **Spring Boot (v3.0.0)**
- **Java 17**

---

<br>

## **End-points**


Registration

<details>

<br>

### **Request**

- URL: `localhost/register`
- Method: **POST**
- Request / response body format: **JSON**

```json
{
    "firstName": "Alberto",
    "lastName": "Delrio",
    "phoneNumber": "+380500010010",
    "ipn": "0000000000",
    "password": "123"
}
```

<br>

### **Response**

- ### 200

```json
{
    "message": "success"
}
```

</details>

<br>

Login

<details>

<br>

### **Request**

- URL: `localhost/login`
- Method: **POST**
- Request / response body format: **JSON**

```json
{
    "phoneNumber": "+380500010010",
    "password": "123"
}
```

<br>

### **Response**

- ### 200

```json
{
    "refreshToken": "jwt",
    "refreshTokenExpiration": "2023-01-20T17:33:14+02:00[Europe/Kiev]",
    "accessToken": "jwt",
    "accessTokenExpiration": "2022-12-21T18:03:14+02:00[Europe/Kiev]",
    "error": null
}
```

- ### 401

```json
{
    "refreshToken": null,
    "refreshTokenExpiration": null,
    "accessToken": null,
    "accessTokenExpiration": null,
    "error": "Password is not correct"
}
```

</details>

<br>

Refresh

<details>

<br>

### **Request**

- URL: `localhost/refresh`
- Method: **POST**
- Request / response body format: **JSON**

```json
{
    "refreshToken": "jwt"
}
```

<br>

### **Response**

- ### 200

```json
{
    "refreshToken": "jwt",
    "refreshTokenExpiration": "2023-01-20T17:33:14+02:00[Europe/Kiev]",
    "accessToken": "jwt",
    "accessTokenExpiration": "2022-12-21T18:03:14+02:00[Europe/Kiev]",
    "error": null
}
```

- ### 400

```json
{
    "refreshToken": null,
    "refreshTokenExpiration": null,
    "accessToken": null,
    "accessTokenExpiration": null,
    "error": "Refresh JWT is fake"
}
```

<br>

### **Request #2**

- URL: `localhost/refresh/logout`
- Method: **POST**
- Request / response body format: **JSON**

```json
{
    "refreshToken": "jwt"
}
```

<br>

### **Response**

- ### 200

```json
{
    "message": "success"
}
```

- ### 400

```json
{
    "error": "Refresh JWT is fake"
}
```

</details>

<br>

Cards

<details>

<br>

### **Request**

- URL: `localhost/user/card/all`
- Method: **GET**

**Authorization header format:**

`Bearer [access_jwt]`


### **Response**

- ### 200

```json
[
  {
    "id": 4,
    "type": "credit",
    "currency": "eur",
    "provider": "mastercard",
    "sum": 1719.14,
    "cardNumber": "5167910743157299",
    "expireDate": "12/24"
  },
  {
    "id": 1,
    "type": "debit",
    "currency": "uah",
    "provider": "mastercard",
    "sum": 953.98,
    "cardNumber": "5167843703217777",
    "expireDate": "12/25"
  }
]
```

<br>

### **Request #2**

- URL: `localhost/user/card/{id}`
- Method: **GET**

**Authorization header format:**

`Bearer [access_jwt]`

### **Response**

- ### 200

```
013
```

- ### 400

```
Card with id:255 not exist
```

</details>