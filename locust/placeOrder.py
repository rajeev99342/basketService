# from locust import HttpUser, TaskSet, task, between
#
# class UserBehavior(TaskSet):
#
#     @task(1)
#     def add_to_cart(self):
#
#     @task(1)
#     def place_order(self):
#         url = 'http://localhost:8080/place-order'
#         data = {
#             "finalAmount": 498,
#             "paymentMode": None,
#             "userPhone": "7903591662",
#             "cartProducts": [
#                 {
#                     "id": 21,
#                     "quantityModel": {
#                         "id": 30
#                     },
#                     "selectedCount": 2,
#                     "cartDetailsId": 137
#                 },
#                 {
#                     "id": 21,
#                     "quantityModel": {
#                         "id": 30
#                     },
#                     "selectedCount": 3,
#                     "cartDetailsId": 137
#                 },
#                 {
#                     "id": 21,
#                     "quantityModel": {
#                         "id": 30
#                     },
#                     "selectedCount": 6,
#                     "cartDetailsId": 137
#                 },
#                 {
#                     "id": 21,
#                     "quantityModel": {
#                         "id": 30
#                     },
#                     "selectedCount": 2,
#                     "cartDetailsId": 137
#                 },
#                 {
#                     "id": 21,
#                     "quantityModel": {
#                         "id": 30
#                     },
#                     "selectedCount": 2,
#                     "cartDetailsId": 137
#                 },
#                 {
#                     "id": 21,
#                     "quantityModel": {
#                         "id": 30
#                     },
#                     "selectedCount": 2,
#                     "cartDetailsId": 137
#                 },
#                 {
#                     "id": 21,
#                     "quantityModel": {
#                         "id": 30
#                     },
#                     "selectedCount": 2,
#                     "cartDetailsId": 137
#                 },
#                 {
#                     "id": 21,
#                     "quantityModel": {
#                         "id": 30
#                     },
#                     "selectedCount": 2,
#                     "cartDetailsId": 137
#                 },
#                 {
#                     "id": 21,
#                     "quantityModel": {
#                         "id": 30
#                     },
#                     "selectedCount": 2,
#                     "cartDetailsId": 137
#                 },
#                 {
#                     "id": 21,
#                     "quantityModel": {
#                         "id": 30
#                     },
#                     "selectedCount": 2,
#                     "cartDetailsId": 137
#                 },
#                 {
#                     "id": 21,
#                     "quantityModel": {
#                         "id": 30
#                     },
#                     "selectedCount": 1,
#                     "cartDetailsId": 137
#                 },
#                 {
#                     "id": 21,
#                     "quantityModel": {
#                         "id": 30
#                     },
#                     "selectedCount": 2,
#                     "cartDetailsId": 137
#                 },
#                 {
#                     "id": 21,
#                     "quantityModel": {
#                         "id": 30
#                     },
#                     "selectedCount": 2,
#                     "cartDetailsId": 137
#                 },
#                 {
#                     "id": 21,
#                     "quantityModel": {
#                         "id": 30
#                     },
#                     "selectedCount": 2,
#                     "cartDetailsId": 137
#                 },
#                 {
#                     "id": 21,
#                     "quantityModel": {
#                         "id": 30
#                     },
#                     "selectedCount": 2,
#                     "cartDetailsId": 137
#                 },
#                 {
#                     "id": 21,
#                     "quantityModel": {
#                         "id": 30
#                     },
#                     "selectedCount": 2,
#                     "cartDetailsId": 137
#                 },
#                 {
#                     "id": 21,
#                     "quantityModel": {
#                         "id": 30
#                     },
#                     "selectedCount": 2,
#                     "cartDetailsId": 137
#                 },
#                 {
#                     "id": 21,
#                     "quantityModel": {
#                         "id": 30
#                     },
#                     "selectedCount": 20,
#                     "cartDetailsId": 137
#                 }
#             ]
#         }
#         headers = {
#             'Accept': 'application/json, text/plain, */*',
#             'Accept-Language': 'en-US,en;q=0.9',
#             'Connection': 'keep-alive',
#             'Content-Type': 'application/json',
#             'DNT': '1'
#         }
#         response = self.client.post(url, headers=headers, json=data, verify=False)
#         if response.status_code == 200:
#             print("API call successful!")
#             print("Response:")
#             print(response.json())
#         else:
#             print("API call failed with status code:", response.status_code)
#             print("Response:")
#             print(response.text)
#
# class WebsiteUser(HttpUser):
#     tasks = [UserBehavior]
#     # wait_time = between(5, 15)
