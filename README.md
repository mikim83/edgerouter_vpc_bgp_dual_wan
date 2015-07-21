# Edgerouter + Edgemax 1.7 + dual WAN failover + AWS VPC VPN Failover + BGP

This is a basic configuration for Edgerouter with EdgeOS 1.7, dual WAN Failover and two AWS VPC VPN connections routed with BGP.

WAN1 : ETH0 with pppoe and static IP

WAN2 : ETH1 with DHCP and dynamic IP

LAN : ETH3


From the VPC - VPN Connections, you can downlad the configuration for Vyatta software and apply it directly to the router, as they are compatible.




