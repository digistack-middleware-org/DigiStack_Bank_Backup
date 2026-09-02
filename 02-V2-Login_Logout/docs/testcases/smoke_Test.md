# Smoke Test Checklist – digistack-bank-v2

| #  | Test Case                | Steps / Location                                                                 | Expected Result                                      | Status |
|----|--------------------------|----------------------------------------------------------------------------------|------------------------------------------------------|--------|
| 1  | Admin Console reachable  | Browser → http://192.168.10.10:9060/ibm/console                                  | Login page loads                                     | ☐      |
| 2  | server1 Started          | Admin Console → Servers → WebSphere application servers                          | Green arrow next to server1                          | ☐      |
| 3  | digistack-bank-v2 Started| Admin Console → Applications → WebSphere enterprise applications                 | Green arrow next to digistack-bank-v2 | ☐      |
| 4  | Home page loads          | Browser → http://192.168.10.10:9080/digistack-bank/Home                          | Page renders, Database: Connected green              | ☐      |
| 5  | Login page loads         | Browser → http://192.168.10.10:9080/digistack-bank/Login                         | Welcome Back card renders                            | ☐      |
| 6  | Login succeeds           | Enter customer1 / Customer@123 → Sign In                                         | Dashboard renders with greeting and last login       | ☐      |
| 7  | Logout works             | Click Logout on Dashboard                                                        | Redirected to Home, session destroyed                | ☐      |
