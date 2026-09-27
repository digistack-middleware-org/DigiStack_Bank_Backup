# Provision dsb-ihs VM + IBM HTTP Server

---

## Step 1 — Create and Configure the dsb-ihs VM in VMware Workstation

### Step 1.1 — Open VMware Workstation

Open **VMware Workstation** on your Windows laptop.

### Step 1.2 — Create a new Virtual Machine

1. Click **File → New Virtual Machine** (or press `Ctrl+N`)
2. Select:
   - **(•) Typical (recommended)**
3. Click **Next**

### Step 1.3 — Guest OS Installation

Select:
- **(•) I will install the operating system later.**

Click **Next**.

### Step 1.4 — Guest Operating System

Select:
- **(•) Linux**

Version dropdown: select **Red Hat Enterprise Linux 8 64-bit**.

Click **Next**.

### Step 1.5 — Name and Location

| Setting | Value |
|---|---|
| Virtual machine name | `dsb-ihs` |
| Location | Your preferred VM storage pathClick **Next**.

### Step 1.6 — Disk Capacity

| Setting | Value |
|---|---|
| Maximum disk size | **20 GB** |
| Disk type | **(•) Store virtual disk as a single file** |

Click **Next**.

### Step 1.7 — Customize Hardware

Click the **Customize Hardware** button **before** Finish, then set:

| Setting | Value |
|---|---|
| Memory | **1024 MB (1 GB)** |
| Processors | **1** |
| New CD/DVD | Browse → select your **RHEL 8 ISO** |
| Network Adapter | **Bridged** (or Host-only, matching your other VMs) |

Click **Close**, then **Finish**.

---

## Step 2 — Install RHEL 8 on dsb-ihs

### Step 2.1

Power on the **dsb-ihs** VM in VMware Workstation.

### Step 2.2 — Follow the RHEL 8 installation wizard

Key settings to set exactly:

| Setting | Value |
|---|---|
| Language | English (United States) |
| Keyboard | English (US) |
| Time zone | Asia/Kolkata (or your local zone) |
| Software Selection | **Minimal Install** |
| Installation Destination | 20 GB disk — automatic partitioning |
| Network & Hostname | Enable the network adapter — set hostname to **dsb-ihs** |
| Root Password | Set a strong root password |
| User Creation | Optional — root access is sufficient for this lab |

### Step 2.3

Complete the installation and **reboot**.

### Step 2.4 — Confirm the hostname

After reboot, log in as root and run:

```bash
hostname
```

**Expected result:**

```
dsb-ihs
```

---

## Step 3 — Configure the Static IP Address

> **Concept — Static IP:** Your project's network standard assigns dsb-ihs the IP address `192.168.10.20`. Setting a static IP ensures this VM always reachable at the same address across reboots — essential because the WAS plugin `plugin-cfg.xml` will reference this IP.

### Step 3.1 — Find the network interface name

```bash
ip addr show
```

> Note the interface name — it will be something like `ens33`, `eth0 or `ens160`. Use the name shown on your VM in all subsequent commands.

### Step 3.2 — Edit the network configuration file

Replace `ens33` with your actual interface name:

```bash
vi /etc/sysconfig/network-scripts/ifcfg-ens33
```

Set the file contents to:

```ini
TYPE=Ethernet
BOOTPROTO=none
NAME=ens33
DEVICE=ens33
ONBOOT=yes
IPADDR=192.168.10.20
NETMASK=255.255.255.0
GATEWAY=192.168.10.1
DNS1=8.8.8.8
```

Save and exit (`:wq`).

### Step 3.3 — Restart the network service

```bash
systemctl restart NetworkManager
```

### Step 3.4 — Confirm the IP address is set correctly

```bash
ip addr show | grep "192.168.10.20"
```

**Expected result:** the IP address `192.168.10.20` appears in the output.

### Step 3.5 — Confirm the VM can reach dsb-dmgr

```bash
ping -c 3 192.168.10.10
```

**Expected result:**

```
3 packets transmitted, 3 received, 0% packet loss
```

---

## Step 4 — Configure the Firewall on dsb-ihs

### Step 4.1 — Open port 80 (HTTP — IHS will listen here)

```bash
firewall-cmd --permanent --add-port=80/tcp
```

### Step 4.2 — Open port 8008 (IHS Admin port)

WAS uses this to communicate with IHS:

```bash
firewall-cmd --permanent --add-port=8008/tcp
```

### Step 4.3 — Reload the firewall

```bash
firewall-cmd --reload
```

**Expected result for each command:**

```
success
```

---

## Step 5 Install IBM Installation Manager on dsb-ihs

> **Concept — IBM Installation Manager (IM):** The same tool used to install WAS ND on dsb-dmgr. You need it on dsb-ihs to install IHS. The IM repository for IHS is typically available your WAS ND installation media or from IBM Passport Advantage.

### Step 5.1 — Locate the installer on dsb-dmgr

On **dsb-dmgr**, find where Installation Manager is installed:

```bash
ls /apps/IBM/InstallationManager/
```

### Step 5.2 — Or transfer the IHS installation package from Windows

If you have the IHS installer package (zip or archive from IBM Fix Central or Passport Advantage), copy it to dsb-ihs using SCP from Windows:

```bash
scp <IHS-installer-package> root@192.168.10.20:/tmp/
```

### Step 5.3 — Install IBM Installation Manager

On **dsb-ihs**:

```bash
cd /tmp/
# Extract your IM package — command varies by downloaded format
# Example for a zip package:
unzip <IM-installer.zip> -d /tmp/IM-installer

# Run the installer
cd /tmp/IM-installer/
./install
```

Follow the IBM Installation Manager GUI (or silent install) accepting defaults. IM installs to `/apps/IBM/InstallationManager/` by default.

### Step 5.4 — Confirm IM is installed

```bash
ls /apps/IBM/InstallationManager/eclipse/tools/imcl
```

**Expected result:** the `imcl` command-line tool is present.

---

## Step 6 — Install IBM HTTP Server 9.0.5.28

### Step 6.1 — Install IHS with imcl

```bash
/apps/IBM/InstallationManager/eclipse/tools/imcl install \
  com.ibm.websphere.IHS.v90 \
  -repositories <path-to-IHS-repository> \
  -installationDirectory /apps/IBM/HTTPServer \
  -acceptLicense
```

> Replace `<path-to-IHS-repository>` with the path to your IHS repository directory (the `repository.config` file location from your IBM download).

### Step 6.2 — Install the WAS Web Server Plug-ins

These are required for the `plugin-cfg.xml mechanism:

```bash
/apps/IBM/InstallationManager/eclipse/tools/imcl install \
  com.ibm.websphere.PLG.v90 \
  -repositories <path-to-plugin-repository> \
  -installationDirectory /apps/IBM/WebSphere/Plugins \
  -acceptLicense
```

### Step 6.3 — Confirm the IHS installation directory exists

```bash
ls /apps/IBM/HTTPServer/
```

**Expected result — directories including:**

```
bin  conf  htdocs  logs  modules  ...
```

### Step 6.4 — Confirm the IHS version

```bash
/apps/IBM/HTTPServer/bin/httpd -v
```

**Expected result:**

```
Server version: IBM_HTTP_Server/9.0.5.28 (Unix)
```

---

## Step 7 — Start IHS and Confirm the Default Page

### Step 7.1 — Start IBM HTTP Server

```bash
/apps/IBM/HTTPServer/bin/apachectl start
```

**Expected result:** no error output. That is correct. ✅

### Step 7.2 — Confirm the IHS process is running

```bash
ps aux | grep httpd
```

**Expected result — one or more `httpd` processes listed:**

```
root     XXXX  0.0  0.1  ... /apps/IBM/HTTPServer/bin/httpd ...
```

### Step 7.3 — Confirm IHS is listening on port 80

```bash
ss -tlnp | grep :80
```

**Expected result:**

```
LISTEN  0  ... 0.0.0.0:80  ...
```

### Step 7.4 — Test from your Windows host browser

Navigate to```
http://192.168.10.20
```

**Expected result:** the IBM HTTP Server default page loads. It will show either:

- `It works!` — or —
- the IBM HTTP Server branded welcome page

Either is correct — both confirm the IHS binary is installed and serving HTTP on port 80. ✅

### Step 7.5 — Confirm in the IHS access log

On the **dsb-ihs** VM:

```bash
tail -5 /apps/IBM/HTTPServer/logs/access_log
```

**Expected result — a log line showing your Windows laptop's IP address making a GET request:**

```
192.168.X.X - - [date] "GET / HTTP/1.1" 200 XXXX
```

---

## Step 8 — Enable IHS to Start Automatically on Boot

```bash
# Create a systemd service for IHS
cat > /etc/systemd/system/ihs.service << 'EOF'
[Unit]
Description=IBM HTTP Server
After=network.target

[Service]
Type=forking
ExecStart=/apps/IBM/HTTPServer/bin/apachectl start
ExecStop=/apps/IBM/HTTPServer/bin/apachectl stop
ExecReload=/apps/IBM/HTTPServer/bin/apachectl graceful
PIDFile=/apps/IBM/HTTPServer/logs/httpd.pid
RemainAfterExit=yes

[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload
systemctl enable ihs
```

**Expected result:**

```
Created symlink /etc/systemd/system/multi-user.target.wants/ihs.service → ...
```
