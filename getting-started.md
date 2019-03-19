For previous releases, please look at the following documentation instead:

- [Getting-started v1](https://github.com/pryv/lib-java/blob/master/getting_started_v1.md)
- [Java example app v1](https://github.com/pryv/app-java-examples/tree/bcfedf62e54ac56cfc71f47bef63282e29222bcb/BasicExample)
- [Android example app v1](https://github.com/pryv/app-android-example/tree/a7ca35203e7030b6ca4ef828096fa85e77bc5aa9)

# Examples

- [Java Basic example: authenticate & retrieve data](https://github.com/pryv/app-java-examples/tree/master/BasicExample)<br>

- [Android App example: authenticate, create note Events and retrieve them, with integration guide](https://github.com/pryv/app-android-example)<br>

# Install the library

In order to import the library in your project, please follow [these instructions](https://github.com/pryv/lib-java/blob/master/README.md#import).

# Authorize your app

First choose an app identifier (REQUESTING_APP_ID, min. length 6 chars), then in your client code:

```java
// Here we request full permissions on a custom stream;
// in practice, scope and permission level will vary depending on your needs
Permission permission = new Permission("example-app-id", Permission.Level.manage, "Example App");
List<Permission> permissions = new ArrayList<Permission>();
permissions.add(permission);

AuthView view = new AuthView() {
	public void onAuthSuccess(String username, String token) {
		// Retrieve username and valid token
	}

	public void onAuthError(String message) {
		// Display error message
	}

	public void onAuthRefused(int reasonId, String message, String detail) {
		// Display authentication refused message
	}

	public void displayLoginView(String loginURL) {
		// Generate WebView to load URL and enter credentials
	}
};

AuthController authenticator = new AuthControllerImpl(REQUESTING_APP_ID, permissions, language, returnURL, view);
authenticator.signIn();
```

See also: [app authorization in the API reference](http://api.pryv.com/reference/#authorizing-your-app)

# Setup connection

```java
Connection connection = new Connection(username, accessToken, domain);
```

# Manage events

## Retrieve

```java
Filter filter = new Filter().addStream('diary');
List<Event> retrievedEvents = connection.events.get(filter);
```

## Create

```java
Event newEvent = new Event()
	.setStreamId("diary")
	.setType("note/txt")
	.setContent("I track, therefore I am.");
newEvent = connection.events.create(newEvent);
```

## Update

```java
newEvent.setContent("updated content");
Event updatedEvent = connection.events.update(newEvent);
```

## Delete

```java
// The first delete will only trash the event
Event trashedEvent = connection.events.delete(newEvent);
trashedEvent.isTrashed(); // true
// The second delete will actually delete the event
Event deletedEvent = connection.events.delete(trashedEvent);
deletedEvent.isDeleted(); // true
```

# Manage Streams

## Retrieve

```java
Filter filter = new Filter().setParentId("myRootStreamId");
Map<String, Stream> retrievedStreams = connection.streams.get(filter);
```

## Create

```java
Stream newStream = new Stream()
	.setId("heartRate")
	.setName("Heart rate");
newStream = connection.streams.create(newStream);
```

## Update

```java
newStream.setName("New name");
Stream updatedStream = connection.streams.update(newStream);
```

## Delete

```java
// The first delete will only trash the stream
Stream trashedStream = connection.streams.delete(newStream, false);
trashedStream.isTrashed(); // true
// The second delete will actually delete the stream
// If mergeEventsWithParent is true, merge the events in the parent stream
// If mergeEventsWithParent is false, also delete the events
Stream deletedStream = connection.streams.delete(trashedStream, mergeEventsWithParent);
deletedStream.isDeleted(); // true
```

# Manage accesses

## Retrieve

```java
List<Access> retrievedAccesses = connection.accesses.get();
```

## Create

```java
Access newAccess = new Access()
	.setName("forMyDoctor")
	.addPermission(new Permission("heartRate", Permission.Level.read, null));
newAccess = connection.accesses.create(newAccess);
```

## Update

```java
newAccess.setName("forMyFamily");
Access updatedAccess = connection.accesses.update(newAccess);
```

## Delete

```java
Access deletedAccess = connection.accesses.delete(newAccess);
deletedAccess.isDeleted(); // true
```

# Handle exceptions

Each of the above calls (get, create, update, delete) may throw `IOExceptions` or
`ApiExceptions` that you may want to catch and handle in your code:

```java
try {
	connection.events.create(newEvent);
}

catch (IOException e) {
	// Handle standard I/O exceptions
}

catch (ApiException e) {
	// Handle exceptions originated from Pryv API
	String errorId = e.getId();
	String errorMsg = e.getMsg();
	String errorData = e.getData();
	ArrayList<String> subErrors = e.getSubErrors();
}
```

# Batch call

Coming soon!

# Further resources

- [API reference](http://api.pryv.com/reference/)
