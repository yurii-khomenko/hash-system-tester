Config
===
Config locate in "conf" dir, file application.conf

 * `samplesCount` (required): The count of requests per one resource
 * `treadsCounts` (required): The count of working treads, ex: [100], [100, 200]
 * `ndcs` (required): The available ndcs
 
 * `name` (required): The developer name
 
 * `hashUrls` (required): The available links for get hash
 * `dehashUrls` (required): The available links for get msisdn
 
 * `user` (required): The user name to your REST API
 * `password` (required): The password to your REST API
 
 * `algorithm` (required): The hashing algorithm, supports: ["MD2", "MD5", "SHA-1", "SHA-224", "SHA-256", "SHA-384", "SHA-512", "SHA3-224", "SHA3-256", "SHA3-384", "SHA3-512"]
 * `salt` (required): The salt. Must be concatenating in rest of msisdn => (msisdn + salt)

Build
===
```
gradle clean installDist assembleDist
```

Run
===

Linux/macOS
```
core/build/install/hash-system-tester/bin/hash-system-tester
```
Windows
```
core\build\install\hash-system-tester\bin\hash-system-tester.bat
```


Results
===
```
[04:11:12.932] [INFO] Khomenko Yurii starts load test for 1000000 samples in 100 threads...
[04:11:18.168] [INFO] 
[04:11:18.168] [INFO] results:
[04:11:18.168] [INFO] 
[04:11:18.168] [INFO] dehash success =>	1000000
[04:11:18.168] [INFO] dehash errors =>	0
[04:11:18.168] [INFO] dehash tps(1) =>	420
[04:11:18.168] [INFO] dehash tps(all) =>	41750
[04:11:18.169] [INFO] dehash p90 =>		2.983ms
[04:11:18.169] [INFO] dehash p99 =>		8.166ms
[04:11:18.169] [INFO] 
[04:11:18.169] [INFO] hash success =>	1000000
[04:11:18.169] [INFO] hash errors =>	0
[04:11:18.169] [INFO] hash tps(1) =>	420
[04:11:18.169] [INFO] hash tps(all) =>	41560
[04:11:18.169] [INFO] hash p90 =>		3.001ms
[04:11:18.169] [INFO] hash p99 =>		8.249ms
[04:11:18.169] [INFO] 
[04:11:18.169] [INFO] hash+dehash success =>	1000000
[04:11:18.169] [INFO] hash+dehash errors =>	0
[04:11:18.169] [INFO] hash+dehash tps(1) =>	210
[04:11:18.170] [INFO] hash+dehash tps(all) =>	20820
[04:11:18.170] [INFO] hash+dehash p90 =>		5.7105ms
[04:11:18.170] [INFO] hash+dehash p99 =>		11.8434ms
[04:11:18.170] [INFO] 
[08:28:51.218] [INFO] top:
[08:28:51.218] [INFO] treads   name    tpsOne tpsAll    p90    p99
[08:28:51.220] [INFO] 100	dehash	420   41750    3ms    8ms
[08:28:51.220] [INFO] 100	hash	420   41560    3ms    8ms
```