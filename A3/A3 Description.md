[Github Repo](https://github.com/XiaoweiZhang-0/CS6650NEU/tree/main/A3)

Some thoughts from the experimentation:

​	The experimentation is done on t3.large ec2 instance and a db.t4g.medium mysql rds

​	My throughputs from numThreadGroups@10, 20, 30 are pretty similar and do not change much for different consumer sizes. It's around 900 per second. Also, my database's cpu utilized rate and number of connections stay low for the most of time. The logs for client records that it frequently encounters socket time out errors on heavier load.  This implies the major bottleneck is on the server side. It could either because of the network or the instance's processing capabillities. Further investigation is needed to improve the throughputs.

​	As for the consumer side, I tested my program for 100, 500, and 1000 consumers. Increasing the consumer number does decrease the average messages in the queue per second. But it was not that impressive. Possible explanations could be as the threads increase, the cost to maintain these threads and switch context offsets the advantage of having more consumer instance. I could not find the best number to keep the queue as short as 0 for a long time. 

I have uploaded my result files to a [google drive](https://drive.google.com/drive/folders/1674-rZ0bPXYk3eaDC3VA6y8n6d4CR8lw?usp=sharing)

I separate my server into two parts, one is a package called consumer and the other is the servlets.

1. Consumer:

   1. Consumer class: this is the class that generates connection to rabbitMQ and consume the messages. It contains the subclass that actually consume the message and the subclass is implemented in a multithreaded way. The subclass follows the RPC procedure to allow the response from database query to be returned to the producer. In the main function, the subclass's instance is instantiated for given number of times
   2. Database class: the class contains postReview method that post review to database, it will throw a SQL exception if posting fails (usually means the album is not found, thus 404 error)

2. Servlet:

   1. DatabaseService:

      1. keeps the two methods to add album and get album by key from database, nothing changed from A2

   2. MusicAlbumServlet:

      1. Same as from A2

   3. ReviewServlet:

      1. Fields:
         1. NOTFOUND: a constant string to compare with the response from database to determine if album is found
         2. logger: debugging purpose
      2. Function:
         1. doPost: 
            1. It will first check if we have two parameters
         2. create a new send instance in the try clause to send the message to the queue and get the response, if the response is album not found, then return 404  error. Else, return success

   4. Send:

      1. Each send represent the producer, it implements Autocloseable so it can be closed in the try-with-resource clause in dopost method in ReviewServlet class

      2. In its constructor, it will initialize the connection factory and make the new connection. Although now I feel like I should initialize the connectionFactory only once in the servlet and pass it as a parameter to the send instance.

      3. In sendMsg function, 

         1. producer will first generate a unique correlation id to correlate the reply with the request
         2. then it will create a temporary queue for the reply message
         3. then in the message properties, correlation ID and the queue are set. Along with the message, the property is sent to the message queue
         4. A consumer is set up to listen on reply queue waiting for the response. Once get it, it will return the reply and the consumer is cancelled.

         

          