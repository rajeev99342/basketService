from locust import HttpUser, TaskSet, task, between
import json  # Import the json module

class UserBehavior(TaskSet):
    def on_start(self):
        self.login()

    def login(self):
        headers = {
            'Accept': 'application/json, text/plain, */*',
            'Accept-Language': 'en-US,en;q=0.9',
            'Authorization': 'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI3OTAzNTkxNjYyIiwiZXhwIjoxNjkyOTc4NzYxLCJpYXQiOjE2OTAzODY3NjF9.i-7wbxMl9ZtetTDK0mp-50TJruZo1o6KIuMgQvUyy6QrJP6sA5v7xQ-urrgedSaWRR5svSLuNUydgNKbUSBcxg',
            'Connection': 'keep-alive',
            'DNT': '1',
            'Origin': 'http://localhost:8100',
            'Referer': 'http://localhost:8100/',
            'User-Agent': 'Mozilla/5.0 (Linux; Android 7.0; SM-G950U Build/NRD90M) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.84 Mobile Safari/537.36',
        }
        response = self.client.get('/fetch-all-product?page=0&size=20', headers=headers, verify=False)

    @task(1)
    def fetch_all_product(self):
        headers = {
            'Accept': 'application/json, text/plain, */*',
            'Accept-Language': 'en-US,en;q=0.9',
            'Authorization': 'Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI3NzU5ODU2OTI3IiwiZXhwIjoxNjkxNDE3ODYyLCJpYXQiOjE2ODg4MjU4NjJ9.m4o7HH_61WV3rtVvebRA3o9oQWV_QDmsOXp4cwaKw_fw_tDVflJtADsZ_klLb0XgLd22nlwKokTZ6S6eQXpjZQ',
            'Connection': 'keep-alive',
            'DNT': '1',
            'Origin': 'http://localhost:8100',
            'Referer': 'http://localhost:8100/',
            'User-Agent': 'Mozilla/5.0 (Linux; Android 7.0; SM-G950U Build/NRD90M) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.84 Mobile Safari/537.36',
        }
        self.client.get('/fetch-all-product?page=0&size=20', headers=headers, verify=False)

class WebsiteUser(HttpUser):
    tasks = [UserBehavior]
    # wait_time = between(5, 15)
