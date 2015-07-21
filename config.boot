firewall {
    all-ping enable
    broadcast-ping disable
    group {
        network-group LAN_NETS {
            network 10.200.0.0/16
            network 172.16.1.0/24
        }
    }
    ipv6-receive-redirects disable
    ipv6-src-route disable
    ip-src-route disable
    log-martians disable
    modify balance {
        rule 10 {
            action modify
            modify {
                lb-group G
            }
        }
    }
    name WAN_IN {
        default-action drop
        description "WAN to internal"
        rule 10 {
            action accept
            description "Allow established/related"
            state {
                established enable
                related enable
            }
        }
        rule 20 {
            action drop
            description "Drop invalid state"
            state {
                invalid enable
            }
        }
    }
    name WAN_LOCAL {
        default-action drop
        description "WAN to router"
        rule 10 {
            action accept
            description "Allow established/related"
            state {
                established enable
                related enable
            }
        }
        rule 20 {
            action drop
            description "Drop invalid state"
            state {
                invalid enable
            }
        }
    }
    options {
        mss-clamp {
            mss 1340
        }
    }
    receive-redirects disable
    send-redirects enable
    source-validation disable
    syn-cookies enable
}
interfaces {
    ethernet eth0 {
        description "Internet - WAN"
        duplex auto
        pppoe 0 {
            default-route auto
            firewall {
                in {
                    name WAN_IN
                }
                local {
                    name WAN_LOCAL
                }
                out {
                }
            }
            mtu 1492
            name-server auto
            password --PRIVATE--
            user-id --PRIVATE--
        }
        speed auto
    }
    ethernet eth1 {
        address dhcp
        description "Internet - WAN 2"
        disable
        duplex auto
        firewall {
            in {
                name WAN_IN
            }
            local {
                name WAN_LOCAL
            }
        }
        speed auto
    }
    ethernet eth2 {
        address 172.16.1.1/24
        description "Testing LAN"
        duplex auto
        firewall {
            in {
                modify balance
            }
        }
        speed auto
    }
    ethernet eth3 {
        address 10.200.1.1/16
        description LAN
        duplex auto
        firewall {
            in {
                modify balance
            }
        }
        speed auto
    }
    ethernet eth4 {
        duplex auto
        speed auto
    }
    ethernet eth5 {
        duplex auto
        speed auto
    }
    ethernet eth6 {
        duplex auto
        speed auto
    }
    ethernet eth7 {
        duplex auto
        speed auto
    }
    loopback lo {
    }
    vti vti0 {
        address INSIDE_CUSTOMER_GATEWAY_1/30
        description "VPC tunnel 1"
        mtu 1436
    }
    vti vti1 {
        address INSIDE_CUSTOMER_GATEWAY_2/30
        description "VPC tunnel 2"
        mtu 1436
    }
}
load-balance {
    group G {
        interface eth1 {
            failover-only
            route-test {
                initial-delay 120
                interval 60
                type {
                    ping {
                        target 8.8.8.8
                    }
                }
            }
        }
        interface pppoe0 {
            route {
                table 1
            }
            route-test {
                initial-delay 120
                interval 60
                type {
                    ping {
                        target 8.8.8.8
                    }
                }
            }
        }
    }
}
port-forward {
    auto-firewall enable
    hairpin-nat enable
    lan-interface eth3
    rule 1 {
        description apache80
        forward-to {
            address 10.200.2.164
            port 80
        }
        original-port 80
        protocol tcp_udp
    }
    rule 2 {
        description apache443
        forward-to {
            address 10.200.1.26
            port 443
        }
        original-port 443
        protocol tcp_udp
    }
    rule 3 {
        description tomcat8080
        forward-to {
            address 10.200.1.26
            port 8080
        }
        original-port 8080
        protocol tcp_udp
    }
    rule 4 {
        description tomcat8090
        forward-to {
            address 10.200.1.26
            port 8090
        }
        original-port 8090
        protocol tcp_udp
    }
    rule 5 {
        description MasterMind
        forward-to {
            address 10.200.1.89
            port 5494
        }
        original-port 5494
        protocol tcp_udp
    }
    rule 6 {
        description OpenVpn
        forward-to {
            address 10.200.1.100
            port 1194
        }
        original-port 1194
        protocol tcp_udp
    }
    rule 7 {
        description ifernandez
        forward-to {
            address 10.200.3.204
            port 18080
        }
        original-port 18080
        protocol tcp_udp
    }
    rule 8 {
        description "VPN pptp"
        forward-to {
            address 10.200.1.17
            port 1723
        }
        original-port 1723
        protocol tcp_udp
    }
    rule 9 {
        description test
        forward-to {
            address 10.200.1.151
            port 2000
        }
        original-port 2000
        protocol tcp_udp
    }
    wan-interface pppoe0
}
protocols {
    bgp 65001 {
        neighbor INSIDE_VIRTUAL_PRIVATE_GATEWAY_1 {
            remote-as 9059
            soft-reconfiguration {
                inbound
            }
            timers {
                holdtime 30
                keepalive 30
            }
        }
        neighbor INSIDE_VIRTUAL_PRIVATE_GATEWAY_2 {
            remote-as 9059
            soft-reconfiguration {
                inbound
            }
            timers {
                holdtime 30
                keepalive 30
            }
        }
        network 10.200.0.0/16 {
        }
    }
    static {
        table 1 {
            interface-route OUTSIDE_VIRTUAL_PRIVATE_GATEWAY_1/32 {
                next-hop-interface pppoe0 {
                }
            }
            interface-route OUTSIDE_VIRTUAL_PRIVATE_GATEWAY_2/32 {
                next-hop-interface pppoe0 {
                }
            }
        }
    }
}
service {
    dhcp-server {
        disabled false
        hostfile-update disable
        shared-network-name LAN {
            authoritative enable
            subnet 172.16.1.0/24 {
                default-router 172.16.1.1
                dns-server 172.16.1.1
                lease 86400
                start 172.16.1.38 {
                    stop 172.16.1.243
                }
            }
        }
        shared-network-name Office {
            authoritative disable
            subnet 10.200.0.0/16 {
                default-router 10.200.1.1
                dns-server 10.200.1.49
                dns-server 10.200.1.50
                lease 86400
                start 10.200.2.10 {
                    stop 10.200.2.254
                }
            }
        }
    }
    dns {
        forwarding {
            cache-size 150
            listen-on eth2
        }
    }
    gui {
        https-port 443
    }
    nat {
        rule 5000 {
            description "masquerade for WAN"
            outbound-interface pppoe0
            type masquerade
        }
        rule 5002 {
            description "masquerade for WAN 2"
            outbound-interface eth1
            type masquerade
        }
    }
    ssh {
        port 22
        protocol-version v2
    }
}
system {
    conntrack {
        expect-table-size 4096
        hash-size 4096
        table-size 32768
        tcp {
            half-open-connections 512
            loose enable
            max-retrans 3
        }
    }
    host-name ubnt
    login {
        user admin {
            authentication {
                encrypted-password --PRIVATE--
                plaintext-password ""
            }
            full-name admin
            level admin
        }
        user ubnt {
            authentication {
                encrypted-password --PRIVATE--
                plaintext-password ""
            }
            full-name ""
            level admin
        }
    }
    name-server 10.200.1.49
    name-server 10.200.1.50
    name-server 8.8.8.8
    ntp {
        server 0.ubnt.pool.ntp.org {
        }
        server 1.ubnt.pool.ntp.org {
        }
        server 2.ubnt.pool.ntp.org {
        }
        server 3.ubnt.pool.ntp.org {
        }
    }
    offload {
        ipsec enable
        ipv4 {
            forwarding enable
            pppoe enable
        }
    }
    syslog {
        global {
            facility all {
                level notice
            }
            facility protocols {
                level debug
            }
        }
    }
    time-zone Europe/Madrid
    traffic-analysis {
        dpi enable
        export enable
    }
}
vpn {
    ipsec {
        auto-firewall-nat-exclude enable
        esp-group AWS {
            compression disable
            lifetime 3600
            mode tunnel
            pfs enable
            proposal 1 {
                encryption aes128
                hash sha1
            }
        }
        ike-group AWS {
            dead-peer-detection {
                action restart
                interval 15
                timeout 30
            }
            key-exchange ikev1
            lifetime 28800
            proposal 1 {
                dh-group 2
                encryption aes128
                hash sha1
            }
        }
        ipsec-interfaces {
            interface eth0
        }
        site-to-site {
            peer OUTSIDE_VIRTUAL_PRIVATE_GATEWAY_1 {
                authentication {
                    mode pre-shared-secret
                    pre-shared-secret --PRIVATE--
                }
                connection-type initiate
                description "VPC tunnel 1"
                ike-group AWS
                local-address OUTSIDE_CUSTOMER_GATEWAY
                vti {
                    bind vti0
                    esp-group AWS
                }
            }
            peer OUTSIDE_VIRTUAL_PRIVATE_GATEWAY_2 {
                authentication {
                    mode pre-shared-secret
                    pre-shared-secret --PRIVATE--
                }
                connection-type initiate
                description "VPC tunnel 2"
                ike-group AWS
                local-address OUTSIDE_CUSTOMER_GATEWAY
                vti {
                    bind vti1
                    esp-group AWS
                }
            }
        }
    }
}


