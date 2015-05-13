// Client.java


import java.io.*;
import java.net.*;
import java.util.Arrays;

public class Client {

   private DatagramPacket sendPacket, receivePacket;
   private DatagramSocket sendReceiveSocket;
   private static BufferedReader inputStream = new BufferedReader 	// Keyboard input
           (new InputStreamReader(System.in)); 
   private char op;
   private String FILENAME;
   private char check = 'Y';
   
   // we can run in normal (send directly to server) or test
   // (send to simulator) mode
   public static enum Mode { NORMAL, TEST};

   public Client()
   {
      try {
         // Construct a datagram socket and bind it to any available
         // port on the local host machine. This socket will be used to
         // send and receive UDP Datagram packets.
         sendReceiveSocket = new DatagramSocket();
      } catch (SocketException se) {   // Can't create the socket.
         se.printStackTrace();
         System.exit(1);
      }
   }

   public void sendAndReceive() throws IOException
   {
	   while (check == 'Y'){
	   System.out.println("Please enter the type of operation R(for read), W(for write)");
	   op = getOperation();
	   System.out.println("Please enter the name of the file");
	   FILENAME = getFileName();
       FileInputStream fis = new FileInputStream(FILENAME);

	   
      byte[] msg = new byte[516], // message we send
             fn, // filename as an array of bytes
             md, // mode as an array of bytes
             data; // reply as array of bytes
      String filename, mode; // filename and mode as Strings
      int j, len, sendPort;
      
      // In the assignment, students are told to send to 68, so just:
      // sendPort = 68; 
      // is needed.
      // However, in the project, the following will be useful, except
      // that test vs. normal will be entered by the user.
      Mode run = Mode.TEST; // change to NORMAL to send directly to server
      
      if (run==Mode.NORMAL) 
         sendPort = 69;
      else
         sendPort = 68;
      
      // sends 10 packets -- 5 reads, 5 writes, 1 invalid
      //for(int i=1; i<=11; i++) {

         System.out.println("Client: creating packet " + "1" + ".");
         
         // Prepare a DatagramPacket and send it via sendReceiveSocket
         // to sendPort on the destination host (also on this machine).

   
         //  opcode for read is 01, and for write 02
        msg[0] = 0;
        if(op == 'R') 
           msg[1]=1;
        else 	// It will always be a read of write, no errors for now
           msg[1]=2;
           
        // File name is taken from input
        filename = FILENAME;
        // convert to bytes
        fn = filename.getBytes();
        
        // and copy into the msg
        System.arraycopy(fn,0,msg,2,fn.length);
        // format is: source array, source index, dest array,
        // dest index, # array elements to copy
        // i.e. copy fn from 0 to fn.length to msg, starting at
        // index 2
        
        // now add a 0 byte
        msg[fn.length+2] = 0;

        // now add "octet" (or "netascii")
        mode = "octet";
        // convert to bytes
        md = mode.getBytes();
        
        // and copy into the msg
        System.arraycopy(md,0,msg,fn.length+3,md.length);
        
        len = fn.length+md.length+4; // length of the message
        // length of filename + length of mode + opcode (2) + two 0s (2)
        // second 0 to be added next:

        // end with another 0 byte 
        msg[len-1] = 0;

        // Construct a datagram packet that is to be sent to a specified port
        // on a specified host.
        // The arguments are:
        //  msg - the message contained in the packet (the byte array)
        //  the length we care about - k+1
        //  InetAddress.getLocalHost() - the Internet address of the
        //     destination host.
        //     In this example, we want the destination to be the same as
        //     the source (i.e., we want to run the client and server on the
        //     same computer). InetAddress.getLocalHost() returns the Internet
        //     address of the local host.
        //  69 - the destination port number on the destination host.
        try {
           sendPacket = new DatagramPacket(msg, len,
                               InetAddress.getLocalHost(), sendPort);
        } catch (UnknownHostException e) {
           e.printStackTrace();
           System.exit(1);
        }

        System.out.println("Client: sending packet " + "i" + ".");
        System.out.println("To host: " + sendPacket.getAddress());
        System.out.println("Destination host port: " + sendPacket.getPort());
        System.out.println("Length: " + sendPacket.getLength());
        System.out.println("Containing: ");
        //for (j=0;j<len;j++) {
          //  System.out.println("byte " + j + " " + msg[j]);
        //}

        // Send the datagram packet to the server via the send/receive socket.

        try {
           sendReceiveSocket.send(sendPacket);
        } catch (IOException e) {
           e.printStackTrace();
           System.exit(1);
        }

        System.out.println("Client: "+ op + "Request sent.");
        
        // if the operation is a write
        if (op == 'W')
        {
        	
        	//construct a new byte for an ACK 0400
        	  data = new byte[4];
              receivePacket = new DatagramPacket(data, data.length);
              
              System.out.println("Client: Waiting for packet.");
              try {
                 // Block until a datagram is received via sendReceiveSocket.
                 sendReceiveSocket.receive(receivePacket);
              } catch(IOException e) {
                 e.printStackTrace();
                 System.exit(1);
              }

              // Process the received datagram.
              System.out.println("Client: ACK received:");
              System.out.println("From host: " + receivePacket.getAddress());
              System.out.println("Host port: " + receivePacket.getPort());
              System.out.println("Length: " + receivePacket.getLength());
              System.out.println("Containing: ");
              for (j=0;j<receivePacket.getLength();j++) {
                  System.out.println("byte " + j + " " + data[j]);
              }
              
              System.out.println();
              
              
              if (data[3] == 0 )
              {
                  System.out.println("1st Ack");

              }
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
              sendData = new byte[MAX_SIZE];
              ///////////////////////////////Send//////////////////////////////
             
              while((count = fis1.read(sendData)) != -1 )
              { 
                  if(noOfPackets<=0)
                      break;
                  
                  
                  
                  System.out.println(new String(sendData));
                  DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getLocalHost(), sendPort);
                  sendReceiveSocket.send(sendPacket);
                  System.out.println("=======");
                  System.out.println("last pack sent" + sendPacket);
                  noOfPackets--;
                  /// Rec ACKS
                  data = new byte[4];
                  receivePacket = new DatagramPacket(data, data.length);

                  System.out.println("Client: Waiting for ACKS.");
                  try {
                     // Block until a datagram is received via sendReceiveSocket.
                     sendReceiveSocket.receive(receivePacket);
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
              DatagramPacket sendPacket1 = new DatagramPacket(lastPack, lastPack.length,InetAddress.getLocalHost(), sendPort);
              sendReceiveSocket.send(sendPacket1);
              System.out.println("last pack sent" + sendPacket1);

          }
      

        	
        

      
        
        ///////////////////////////////Reading//////////////////////////////////////////////
        else if (op == 'R'){
        data = new byte[516];
        receivePacket = new DatagramPacket(data, data.length);
        FileOutputStream out = new FileOutputStream("clientOutput.txt");

        System.out.println("Client: Waiting for packet.");
        try {
           // Block until a datagram is received via sendReceiveSocket.
           sendReceiveSocket.receive(receivePacket);
        } catch(IOException e) {
           e.printStackTrace();
           System.exit(1);
        }
        int chck =0;
       // int count=1, count2 =0;
        while (chck ==0)
        {
       	 // Waiting for Data 
       	 
       	 byte[] data2 = new byte[512];
            receivePacket = new DatagramPacket(data, data.length);

           System.out.println("Server: Waiting for packet.");
            // Block until a datagram packet is received from receiveSocket.
            try {
            	sendReceiveSocket.receive(receivePacket);
            } catch (IOException e) {
               e.printStackTrace();
               System.exit(1);
            }
            System.arraycopy(data,4,data2,0,data2.length);
            // Write Packet to file
            writeToFile(data2,out);
            
           
          
            if (data2[511]==0)
            {
           	 chck =1;
           	 //out.close();
            }
        }

        // Process the received datagram.
        System.out.println("Client: Packet received:");
        System.out.println("From host: " + receivePacket.getAddress());
        System.out.println("Host port: " + receivePacket.getPort());
        System.out.println("Length: " + receivePacket.getLength());
        System.out.println("Containing: ");
        for (j=0;j<receivePacket.getLength();j++) {
            System.out.println("byte " + j + " " + data[j]);
        }
        
        }
        ////////////////////////////////////////////////////////////////////////////////////////
        System.out.println();
        
        System.out.println("Would you like to do another file transfer ? Y/N");
        check = getOperation();

      }

       System.out.println("Closing Client , GoodBye");


      // We're finished, so close the socket.
      sendReceiveSocket.close();
   }
   /* Get a char from the user and return it */
   public static char getOperation() { 
		try {
			String in = inputStream.readLine().trim();
			if (in.length() == 0)
				return (char)0;
			else
           	return (in.charAt(0));
		} catch (Exception e) {
	    	e.printStackTrace();
	    	return(char)0;
		}
   }
   
   /* Get a string of text from the user and return it */
   public static String getFileName() { 
		try {
           return inputStream.readLine();
		} catch (Exception e) {
	    	e.printStackTrace();
	    	return "";
		}
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

   public static void main(String args[]) throws IOException
   {
      Client c = new Client();
      c.sendAndReceive();
   }
}

