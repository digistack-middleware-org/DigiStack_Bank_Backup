## Step 1 — Build the New VM (dsb-node02)

# Create Node02 and Create Custom Profile
### Step 1.1 — Create the Empty VM

In VMware:

```
File → New Virtual Machine → Typical → "Install OS later"
→ Linux → Red Hat Enterprise Linux 8 (64-bit)
→ Name it dsb-node02
```

> 💡 *"Install OS later"* means: build the empty machine first, put the operating system in afterward.

### Step 1.2 — Give It Hardware

| Item | Value | Why |
|---|---|---|
| RAM | 2048 MB (2 GB) | Enough for WAS in a lab |
| CPU | 2 | Enough processing power |
| Disk | 40 GB | Space for RHEL + WAS |
| Network | Same as other VMs | So all machines can talk to each other |

### Step 1.3 — Install RHEL 8

- **Hostname:** `dsb-node02`
- Choose **minimal install** (only basics — no extra software)
- Set a **root password**

### Step 1.4 — Give It a Fixed Address (IP: 192.168.10.11)

A **static IP** means the machine **always has the same address** — like a fixed house address so others can always find it.

Edit the network file:

```bash
vi /etc/sysconfig/network-scripts/ifcfg-ens33
```

Put this inside:

```ini
TYPE=Ethernet
BOOTPROTO=none
NAME=ens33
DEVICE=ens33
ONBOOT=yes
IPADDR=192.168.10.11
NETMASK=255.255.255.0
GATEWAY=192.168.10.1
DNS1=8.8.8.8
```

Then restart networking and test:

```bash
systemctl restart NetworkManager
ping -c 3 192.168.10.10
```

✅ **Expected:** `0% packet loss` — meaning dsb-node02 can successfully "talk" to dsb-dmgr (192.168.10.10).

### Step 1.5 — Open the Firewall Doors

A firewall is like a **security guard at the door**. By default it blocks visitors. WAS uses specific "doors" (ports) to communicate, so we open them:

| Port | Used for |
|---|---|
| 9060 | Admin console |
| 9080 | App traffic |
| 8878 / 8879 | Node talking to DMgr (SOAP connector) |

```bash
firewall-cmd --permanent --add-port=9060/tcp
firewall-cmd --permanent --add-port=9080/tcp
firewall-cmd --permanent --add-port=8878/tcp
firewall-cmd --permanent --add-port=8879/tcp
firewall-cmd --reload
```

---

## Step 2 — Install WebSphere ND on dsb-node02

### Step 2.1 — Install the Software

Install **WebSphere ND** using **IBM Installation Manager** — the **same way you did on dsb-dmgr** — into:

```
/apps/IBM/WebSphere/AppServer/
```

> 💡 Every node must have the **same software, same version, same path** — so the whole cluster is uniform.

### Step 2.2 — Check the Version

```bash
/apps/IBM/WebSphere/AppServer/bin/versionInfo.sh
```

✅ **Expected:** `Version 9.0.5.28` — must match dsb-dmgr exactly.

# Create the Custom Profile 

Step 2.1 — First create a standalone AppServer profile on dsb-node02 so there is a node to federate. On dsb-node02:
```
bash
/apps/IBM/WebSphere/AppServer/bin/manageprofiles.sh -create \
  -profileName devdsbinappserver02 \
  -profilePath /apps/IBM/WebSphere/AppServer/profiles/devdsbinappserver02 \
  -templatePath /apps/IBM/WebSphere/AppServer/profileTemplates/default \
  -serverName server1 \
  -cellName devdsbincell01temp \
  -nodeName devdsbinnode02 \
  -adminUserName wasadmin \
  -adminPassword <YourPassword> \
  -enableAdminSecurity true
```
Expected result:
```
INSTCONFSUCCESS: Success: Profile devdsbinappserver02 now exists.
```