#include<stdio.h> 				//For standard things
#include<stdlib.h>    			//malloc
#include<string.h>    			//memset
#include<netinet/in.h>
// #include<netinet/ip_icmp.h>  //Provides declarations for icmp header
// #include<netinet/udp.h>   	//Provides declarations for udp header
// #include<netinet/tcp.h>   	//Provides declarations for tcp header
#include<netinet/ip.h>    		//Provides declarations for ip header
#include<netinet/if_ether.h>  	//For ETH_P_ALL
#include<sys/socket.h>			// Socket
#include<unistd.h>				// For close()-Method of Socket
#include<sys/ioctl.h>
#include<arpa/inet.h>
#include<time.h>				// To create Timestamp of Packets

void ProcessPacket(unsigned char* , int);
void print_packet(unsigned char* , int);
void flush_buffer();

int sock_raw;
char* serverAddress;
struct sockaddr_in source, dest, saddr;
struct timespec tv;
struct tm * ptm;
unsigned char *buffer;


int main(int argc, char* argv[])
{
	// Overgiven Argument is the address for the Sniffer to get Packets from
	serverAddress = argv[1];
    unsigned char *buffer = (unsigned char *)malloc(65536); //Its Big!

    printf("Starting...\n");
    flush_buffer();
    //Create a raw socket that shall sniff
    sock_raw = socket( PF_PACKET , SOCK_RAW , htons(ETH_P_ALL));

    if(sock_raw < 0)
    {
        printf("Socket Error\n");
        flush_buffer();
        return 1;
    }

    unsigned int saddr_size;
    int data_size;
    struct sockaddr saddr;

    while(1)
    {
        saddr_size = sizeof saddr;
        // Receive a packet
        data_size = recvfrom(sock_raw , buffer , 65535, 0, &saddr , &saddr_size);
        if(data_size <0 )
        {
            printf("Recvfrom error , failed to get packets\n");
            flush_buffer();
            return 1;
        }
        // Now process the packet
        ProcessPacket(buffer , data_size);
    }
    close(sock_raw);
    printf("Finished");
    flush_buffer();
    return 0;
}

// Distinguish different Protocols
void ProcessPacket(unsigned char* buffer, int size)
{
	//Get the IP Header part of this packet , excluding the ethernet header
	struct iphdr *iph = (struct iphdr*)(buffer + sizeof(struct ethhdr));

	switch (iph->protocol) //Check the Protocol and do accordingly...
	{
        case 1:  //ICMP Protocol
            //PrintIcmpPacket(Buffer,Size);
            break;

        case 2:  //IGMP Protocol
            break;

        case 6:  //TCP Protocol
        	// Print relevant information
            print_packet(buffer, size);
            break;

        case 17: //UDP Protocol
            // print_udp_packet(buffer , size);
            break;

        default: //Some Other Protocol like ARP etc.
            break;
    }
}

// Method to print relevant information of the Packet
void print_packet(unsigned char* Buffer, int Size)
{
    struct iphdr *iph = (struct iphdr *)(Buffer  + sizeof(struct ethhdr) );

    // Get Source-IP and Destination-IP
    memset(&source, 0, sizeof(source));
    source.sin_addr.s_addr = iph->saddr;

    memset(&dest, 0, sizeof(dest));
    dest.sin_addr.s_addr = iph->daddr;

    // Save Source-IP and Destination-IP in Variables
    char *temp = inet_ntoa(dest.sin_addr);
    char *d = (char*)malloc(strlen(temp) + 1);
    strcpy(d, temp);
    temp = inet_ntoa(source.sin_addr);
    char *s = (char*)malloc(strlen(temp) + 1);
    strcpy(s, temp);

    // Check Constraints for the Packets
    if((strcmp(d, serverAddress) == 0 || strcmp(s, serverAddress) == 0) && Size >= 100)
        {
    		// Get Timestamp of last Packet
        	ioctl(sock_raw, SIOCGSTAMPNS, &tv);
        	ptm = gmtime(&tv.tv_sec);
        	// Print all relevant Information
        	printf("%s-%s-%d-%d.%d.%d,%d:%d:%d.%ld\n", s, d, Size,
        	ptm->tm_mday, (ptm->tm_mon + 1), (1900 + ptm->tm_year), (ptm->tm_hour + 1), ptm->tm_min, ptm->tm_sec, tv.tv_nsec);
        	flush_buffer();
        }
}

// to flush the stdout Buffer
void flush_buffer()
{
	fflush(stdout);
}
