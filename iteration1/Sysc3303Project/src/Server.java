// TFTPServer.java
// This class is the server side of a simple TFTP server based on
// UDP/IP. The server receives a read or write packet from a client and
// sends back the appropriate response without any actual file transfer.
// One socket (69) is used to receive (it stays open) and another for each response. 

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {

   // types of requests we can receive
   public static enum Request { READ, WRITE, ERROR};
   // responses for valid requests
   public static final byte[] readResp = {0, 3, 0, 1};
   public static final byte[] writeResp = {0, 4, 0, 0};
   
   // UDP datagram packets and sockets used to send / receive
   private DatagramPacket sendPacket, receivePacket;
   private DatagramSocket receiveSocket, sendSocket;
   private String FILENAME = "test.txt";
   FileInputStream fis = null;

   
   
   public Server()
   {
      try {
         // Construct a datagram socket and bind it to port 69
         // on the local host machine. This socket will be used to
         // receive UDP Datagram packets.
         receiveSocket = new DatagramSocket(69);
      } catch (SocketException se) {
         se.printStackTrace();
         System.exit(1);
      }
   }

   public void receiveAndSendTFTP() throws Exception
   {

      byte[] data,
             response = new byte[4];
      
      Request req; // READ, WRITE or ERROR
      
      String filename, mode;
      int len, j=0, k=0;

      for(;;) { // loop forever
         // Construct a DatagramPacket for receiving packets up
         // to 100 bytes long (the length of the byte array).
         
         data = new byte[516];
         receivePacket = new DatagramPacket(data, data.length);

         System.out.println("Server: Waiting for packet.");
         // Block until a datagram packet is received from receiveSocket.
         try {
            receiveSocket.receive(receivePacket);
         } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
         }

         // Process the received datagram.
         System.out.println("Server: Packet received:");
         System.out.println("From host: " + receivePacket.getAddress());
         System.out.println("Host port: " + receivePacket.getPort());
         System.out.println("Length: " + receivePacket.getLength());
         System.out.println("Containing: " );
         
         // print the bytes
         for (j=0;j<receivePacket.getLength();j++) {
            System.out.println("byte " + j + " " + data[j]);
         }

         // Form a String from the byte array.
         String received = new String(data,0,receivePacket.getLength());
         System.out.println(received);

         // If it's a read, send back DATA (03) block 1
         // If it's a write, send back ACK (04) block 0
         // Otherwise, ignore it
         if (data[0]!=0) req = Request.ERROR; // bad
         else if (data[1]==1) 
        	 {
        	 req = Request.READ; // could be read
        	 
        	 }
         else if (data[1]==2) req = Request.WRITE; // could be write
         else req = Request.ERROR; // bad
         
         len = receivePacket.getLength();

         if (req!=Request.ERROR) { // check for filename
             // search for next all 0 byte
             for(j=2;j<len;j++) {
                 if (data[j] == 0) break;
            }
            if (j==len) req=Request.ERROR; // didn't find a 0 byte
            // otherwise, extract filename
            filename = new String(data,2,j-2);
         }
 
         if(req!=Request.ERROR) { // check for mode
             // search for next all 0 byte
             for(k=j+1;k<len;k++) { 
                 if (data[k] == 0) break;
            }
            if (k==len) req=Request.ERROR; // didn't find a 0 byte
            mode = new String(data,j,k-j-1);
         }
         
         if(k!=len-1) req=Request.ERROR; // other stuff at end of packet        
         
         /////////////////////////////////////////Reading/////////////////////////////////
         // Create a response.
         if (req==Request.READ)  // for Read it's 0301
        	 {
        		 
        	   try {
        		    fis = new FileInputStream(FILENAME);
        	   }catch(FileNotFoundException e)
        	   {
        		   fis.close();}
           response = readResp;
           
           ///////////////////////////////
           ///////////////// Calculating length of file and last packet size////////////////////
           int count =0;
           int totLength =0;
           int MAX_SIZE = 512;
           byte[] sendData = new byte[MAX_SIZE];

           
           while((count = fis.read(sendData)) != -1)    //calculate total length of file
           {
               totLength += count;
             for (j=0;j<MAX_SIZE;j++) {
                 System.out.println(" : " + j + " " + sendData[j]);
              }

           }
           
           System.out.println("Total Length :" + totLength);

           int noOfPackets = totLength/MAX_SIZE;
           System.out.println("No of packets : " + noOfPackets);

           int off = noOfPackets * MAX_SIZE;  //calculate offset

           int lastPackLen = totLength - off;	// real last packt length
           System.out.println("\nLast packet Length : " + lastPackLen);

           byte[] lastPack = new byte[lastPackLen-1];  //create new array without redundant information
           /////////////////////////////
           
           fis.close();

           FileInputStream fis1 = new FileInputStream(FILENAME);
         // MAX_SIZE = 516;
          byte[] Data = new byte[MAX_SIZE];
           sendData = new byte[516];
           int cnt = 1;
           int count2 = 0;
           ///////////////////////////////Send//////////////////////////////
          
           while((count = fis1.read(Data)) != -1 )
           { 
               if(noOfPackets<=0)
                   break;
               
               
               
               //System.out.println(new String(sendData));
               sendData[0]= 0;
               sendData[1] =3;
               sendData[2]= (byte)(count2%10);
               sendData[3]= (byte)(cnt%10);
               
               for (int i=0; i<MAX_SIZE;i++)
               {
            	   sendData[i+4]= Data[i];
               }
               
              
               
               DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getLocalHost(), 68);
               sendSocket.send(sendPacket);
               System.out.println("========");
               System.out.println("last pack sent" + sendPacket);
               noOfPackets--;
               cnt++;
               if (cnt%10 ==0 )
              	 count2++;
               /// Rec ACKS
              // data = new byte[4];
              // receivePacket = new DatagramPacket(data, data.length);

               //System.out.println("Client: Waiting for ACKS.");
               try {
                  // Block until a datagram is received via sendReceiveSocket.
                  sendSocket.receive(receivePacket);
               } catch(IOException e) {
                  e.printStackTrace();
                  System.exit(1);
               }
               
               
           
           }

           //check
           System.out.println("\nlast packet\n");
           System.out.println(new String(sendData));

           lastPack = Arrays.copyOf(sendData, lastPackLen);

           System.out.println("\nActual last packet\n");
           System.out.println(new String(lastPack));
                   //send the correct packet now. but this packet is not being send. 
           DatagramPacket sendPacket1 = new DatagramPacket(lastPack, lastPack.length,InetAddress.getLocalHost(), 68);
           sendSocket.send(sendPacket1);
           System.out.println("last pack sent" + sendPacket1);
           ////////////////////////////////////////////////////////////////////
           
           
           
           
           
            
            ////////////////////Write///////////////////
         } else if (req==Request.WRITE) { // for Write it's 0400
        	 {
        		 FileOutputStream out = new FileOutputStream("serverOutput.txt");
                 response = writeResp;
                 sendPacket = new DatagramPacket(response, response.length,		// 1st ACK
                         receivePacket.getAddress(), receivePacket.getPort());
                 // Send the datagram packet to the client via a new socket.

                 try {
                    // Construct a new datagram socket and bind it to any port
                    // on the local host machine. This socket will be used to
                    // send UDP Datagram packets.
                    sendSocket = new DatagramSocket();
                 } catch (SocketException se) {
                    se.printStackTrace();
                    System.exit(1);
                 }

                 try {
                    sendSocket.send(sendPacket);
                 } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(1);
                 }
                 System.out.println("Server: packet sent using port " + sendSocket.getLocalPort());
                 System.out.println();
                 
                 int chck =0;
                 int count=1, count2 =0;
                 while (chck ==0)
                 {
                	 // Waiting for Data 
                	 
                	 data = new byte[512];
                     receivePacket = new DatagramPacket(data, data.length);

                    System.out.println("Server: Waiting for packet.");
                     // Block until a datagram packet is received from receiveSocket.
                     try {
                        receiveSocket.receive(receivePacket);
                     } catch (IOException e) {
                        e.printStackTrace();
                        System.exit(1);
                     }
                     // Write Packet to file
                     writeToFile(data,out);
                     
                      byte[] resp = {0, 4, (byte) (count2%10),(byte)(count%10)};
                     //////////////////////// Sending ACKsss//////////////////////////////////////
                     sendPacket = new DatagramPacket(resp, response.length,		
                             receivePacket.getAddress(), receivePacket.getPort());
                     // Send the datagram packet to the client via a new socket.

                     try {
                        // Construct a new datagram socket and bind it to any port
                        // on the local host machine. This socket will be used to
                        // send UDP Datagram packets.
                        sendSocket = new DatagramSocket();
                     } catch (SocketException se) {
                        se.printStackTrace();
                        System.exit(1);
                     }

                     try {
                        sendSocket.send(sendPacket);
                     } catch (IOException e) {
                        e.printStackTrace();
                        System.exit(1);
                     }
                     System.out.println("Server: packet sent using port " + sendSocket.getLocalPort());
                     System.out.println();
                     count++;
                     if (count%10 ==0 )
                    	 count2++;
                     /////////////////////////////////////////
                     if (data[511]==0)
                     {
                    	 chck =1;
                    	 //out.close();
                     }
                 }
                	 
                 
                 
                 
                 

                 
                 
        	 }
        	 ///////////////////////////////////////////////////////////////////////
         } else { // it was invalid, just quit
            throw new Exception("Not yet implemented");
         }

         
         // Construct a datagram packet that is to be sent to a specified port
         // on a specified host.
         // The arguments are:
         //  data - the packet data (a byte array). This is the response.
         //  receivePacket.getLength() - the length of the packet data.
         //     This is the length of the msg we just created.
         //  receivePacket.getAddress() - the Internet address of the
         //     destination host. Since we want to send a packet back to the
         //     client, we extract the address of the machine where the
         //     client is running from the datagram that was sent to us by
         //     the client.
         //  receivePacket.getPort() - the destination port number on the
         //     destination host where the client is running. The client
         //     sends and receives datagrams through the same socket/port,
         //     so we extract the port that the client used to send us the
         //     datagram, and use that as the destination port for the TFTP
         //     packet.

         sendPacket = new DatagramPacket(response, response.length,
                               receivePacket.getAddress(), receivePacket.getPort());

         System.out.println("Server: Sending packet:");
         System.out.println("To host: " + sendPacket.getAddress());
         System.out.println("Destination host port: " + sendPacket.getPort());
         System.out.println("Length: " + sendPacket.getLength());
         System.out.println("Containing: ");
         for (j=0;j<response.length;j++) {
            System.out.println("byte " + j + " " + response[j]);
         }

         // Send the datagram packet to the client via a new socket.

         try {
            // Construct a new datagram socket and bind it to any port
            // on the local host machine. This socket will be used to
            // send UDP Datagram packets.
            sendSocket = new DatagramSocket();
         } catch (SocketException se) {
            se.printStackTrace();
            System.exit(1);
         }

         try {
            sendSocket.send(sendPacket);
         } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
         }

         System.out.println("Server: packet sent using port " + sendSocket.getLocalPort());
         System.out.println();

         // We're finished with this socket, so close it.
         sendSocket.close();
      } // end of loop

   }
   
   public static void writeToFile(byte[] data, FileOutputStream out) throws IOException
	{
	     try {
	            
	            for(int i=0;i<data.length;i++)
	            	out.write(data[i]);
			} catch(FileNotFoundException e) 
			{
	            if (out != null) 
	            {
	                out.close();
	            }
	            
	        }
		
	}

   public static void main( String args[] ) throws Exception
   {
      Server c = new Server();
      c.receiveAndSendTFTP();
   }
}

