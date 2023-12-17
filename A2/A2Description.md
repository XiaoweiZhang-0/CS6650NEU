[Github Repo](https://github.com/XiaoweiZhang-0/CS6650NEU/tree/main/A2)

Data model

My database includes a table called albums which consist of 5 columns

1. ID: String
2. Artist: String
3. image: BLOB
4. title: String
5. year: String

I'm using AWS RDS MySQL database in my implementation and my server side contains a database connection pool that controls the total connections to the database from the servlet. After various testing, I set the maximum number of total connections to 30, maximum idle connections to 5, and minimum idle connections to 2.

Note to get the valid key for new album, I'm using a random key generation method with the help of Java.Util.UUID. Although the possibility of key coincidence is not zero, it greatly simplifies the way how multiple instances of server get distinct key. I have tried to use the auto-incrementing feature of mySQL database but limited by its computing resources, relying on database to generate the key greatly limit the performance. 

Realizing the bottleneck is the database IO, for the fine-tune, I upgrade the database from db.t3.micro to db.t4g.medium with storage optimization feature. For faster sql query from larger database, I also created an index for database, which is the id of the album. 

On server side, I added auto-scaling functionality to the load balancer which enables at most 5 t2.large instances to run concurrently. 

One fun fact is that before and after I did the fine-tune, my database cpu both kept clock to 90% on heavy load. I think it is because the added EC 2 instances increase the server side processing rate, which leads to database bottleneck again.

In the end, I got 200% throughput increase in the fine-tuned run, compared to the two ec2 instances run. It's also a 365% throughput increase compared to the single instance run. All comparisons are made based on same client side configuration. 

[Link to report Pics](https://drive.google.com/drive/folders/1FUMq_2ZfUO3XnT8SbmgBsK-U5UaMjQY2?usp=sharing)

