
- [Call to library authors](#call)
- [Examples](#examples)
  - [A signup verticle](#signup)

## <a name="call"><a/> Call to library authors

Be the first real user of the libraries you design and develop. It will improve the library, and it will be
the best documentation.

## <a name="examples"><a/> Examples
### <a name="signup"><a/> A signup verticle

The verticle receives a Json conforming to the following [json-spec](https://github.com/imrafaelmerino/json-values):

```

JsObjSpec.strict("age", integer.optional(),
                 "name", str,
                 "registration_date", instant,
                 "image", binary.optional(),
                 "email", JsObjSpec.strict("validated",boolean
                                           "address", str
                                           ),
                 "address", str 
                );
                
```                

The address introduced by the user is a string. The service must call the Geocode API from Google to validate and normalize
the address. The addresses returned by Google are sent back to the frontend (the final user will have to choose one or reject
all of them, but this falls out of the scope of the example). It returns an empty array if an error happens.

The service must persist the client information in a MongoDB. The id returned by MongoDB will be the client identifier
and must be sent back to the frontend. If the client is persisted successfully, the service sends an email to the user
if their email is not already validated (flag email.validated=false). **The service must send the email asynchronously**.

The number of clients is also returned so that the frontend can show a welcoming fancy message to the final user of the
kind "you're the user number 3000!". If an error happens, -1 is returned, and the frontend won't show the message.

**The signup service must execute all the operations in parallel**: the request to  Google and the MongoDB calls (persist and count)

The service response conforms to the following json-spec:

```

JsObjSpec.strict("users", integer
                 "id", str,
                 "timestamp", instant,
                 "addresses", array
                )
                
```                

where the timestamp is the instant when the frontend request gets to the server.

**All the operations must be resilient under certain errors**. The programmer should parametrize the errors, the number
of retries, and the time between each attempt (retry policy).

This example sets up a http server and a telnet server listening on the ports 7890 and 4000.

The telnet server can be used to interact from the shell console with those verticles that takes a string as input
messages. For example:

```
telnet localhost 4000

bus-send --reply find_one_client_by_email imrafaelmerino@gmail.com

```


