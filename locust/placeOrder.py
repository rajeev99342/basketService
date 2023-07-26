from locust import HttpUser, TaskSet, task, between
import json

base_url = "http://154.49.243.177:9000"

token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI3OTAzNTkxNjYyIiwiZXhwIjoxNjkyOTc4NzYxLCJpYXQiOjE2OTAzODY3NjF9.i-7wbxMl9ZtetTDK0mp-50TJruZo1o6KIuMgQvUyy6QrJP6sA5v7xQ-urrgedSaWRR5svSLuNUydgNKbUSBcxg"
userPhone = '7903591662'
class UserBehavior(TaskSet):


    @task(1)
    def add_to_cart(self):
        url = base_url + '/add-to-cart'
        headers = {
            'Accept': 'application/json, text/plain, */*',
            'Accept-Language': 'en-US,en;q=0.9',
            'Authorization': f'Bearer {token}',
            'Connection': 'keep-alive',
            'Content-Type': 'application/json'

        }
        rq_array = self.prepare_cart_rq()
        for data in rq_array:
            response = self.client.post(url, headers=headers, json=data,verify=False)
            if response.status_code == 200:
                print("ADD SUCCESSFULL")
            else:
                print("API call failed. Status code:", response.status_code)

        print("Few product added into cart")
        self.fetch_cart_list()

    def prepare_cart_rq(self):
        data = [
            {
                "productId": 1,
                "userPhone": {userPhone},
                "selectedProductCount": 1,
                "quantityId": 1
            },
            {
                "productId": 2,
                "userPhone": {userPhone},
                "selectedProductCount": 1,
                "quantityId": 2
            },
            {
                "productId": 3,
                "userPhone": {userPhone},
                "selectedProductCount": 1,
                "quantityId": 3
            },
            {
                "productId": 5,
                "userPhone": {userPhone},
                "selectedProductCount": 1,
                "quantityId": 5
            },
            {
                "productId": 6,
                "userPhone": {userPhone},
                "selectedProductCount": 1,
                "quantityId": 17
            },
            {
                "productId": 7,
                "userPhone": {userPhone},
                "selectedProductCount": 1,
                "quantityId": 7
            },
            {
                "productId": 8,
                "userPhone": {userPhone},
                "selectedProductCount": 1,
                "quantityId": 8
            },
            {
                "productId": 9,
                "userPhone": {userPhone},
                "selectedProductCount": 1,
                "quantityId": 9
            },
            {
                "productId": 10,
                "userPhone": {userPhone},
                "selectedProductCount": 1,
                "quantityId": 10
            },
            {
                "productId": 11,
                "userPhone": {userPhone},
                "selectedProductCount": 1,
                "quantityId": 11
            },
            {
                "productId": 12,
                "userPhone": {userPhone},
                "selectedProductCount": 1,
                "quantityId": 12
            },
            {
                "productId": 13,
                "userPhone": {userPhone},
                "selectedProductCount": 1,
                "quantityId": 13
            },
            {
                "productId": 14,
                "userPhone": {userPhone},
                "selectedProductCount": 1,
                "quantityId": 14
            },
            {
                "productId": 15,
                "userPhone": {userPhone},
                "selectedProductCount": 1,
                "quantityId": 15
            },
            {
                "productId": 4,
                "userPhone": {userPhone},
                "selectedProductCount": 1,
                "quantityId": 4
            }
        ]
        print("====>>>> ADD TO CART")
        print(data)
        return data

    @task(1)
    def fetch_cart_list(self):
        url = base_url + '/fetch-product-by-cart'
        headers = {
                'Accept': 'application/json, text/plain, */*',
                'Accept-Language': 'en-US,en;q=0.9',
                'Authorization': f'Bearer {token}',
                'Connection': 'keep-alive',
                'DNT': '1'
            }
        params = {
            'token': token
        }

        response = self.client.get(url, headers=headers,params=params,verify=False)
        if response.status_code == 200:
            print("##################>> API call successful!")
            print(response.content)
            res_data = json.loads(response.content)
            place_order_data = res_data['body']
            print(place_order_data)
            extracted_data = []
            for item in place_order_data:
                quantityModel = {"id":item["quantityModel"]["id"]}
                extracted_item = {
                    "id": item["model"]["id"],
                    "quantityModel":quantityModel,
                    "selectedCount": item["selectedCount"],
                    "cartDetailsId": item["cartDetailsId"]
                }

                extracted_data.append(extracted_item)
            print('----------------->>> ')
            print(extracted_data)
            self.place_order(self,extracted_data)
        else:
            print("API call failed. Status code:", response.status_code)
    @task(1)
    def place_order(self,extracted_data):
        url = base_url + '/place-order'
        data = {
            "finalAmount": 498,
            "paymentMode": None,
            "userPhone": "7903591662",
            "cartProducts": extracted_data
        }
        headers = {
            'Accept': 'application/json, text/plain, */*',
            'Accept-Language': 'en-US,en;q=0.9',
            'Connection': 'keep-alive',
            'Content-Type': 'application/json',
            'DNT': '1'
        }
        response = self.client.post(url, headers=headers, json=data, verify=False)
        if response.status_code == 200:
            print("API call successful!")
            print("Response:")
            print(response.json())
        else:
            print("API call failed with status code:", response.status_code)
            print("Response:")
            print(response.text)

class WebsiteUser(HttpUser):
    tasks = [UserBehavior]
    # wait_time = between(5, 15)
