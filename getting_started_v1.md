### Install the library

In order to import the library in your project, please follow [these instructions](https://github.com/pryv/lib-java/blob/master/README.md#import).

### Authorize your app

First choose an app identifier (REQUESTING_APP_ID, min. length 6 chars), then in your client code:

```java
// Here we request full permissions on a custom stream;
// in practice, scope and permission level will vary depending on your needs
Permission permission = new Permission("example-app-id", Permission.Level.manage, "Example App");
List<Permission> permissions = new ArrayList<Permission>();
permissions.add(permission);

AuthView view = new AuthView() {
	public void onAuthSuccess(String username, String token) {
      // provides username and valid token
      ...
    }

    public void onAuthError(String message) {
      // display error message
      ...
    }

    public void onAuthRefused(int reasonId, String message, String detail) {
  	  // display authentication refused message
  	  ...
    }

    public void displayLoginView(String loginURL) {
      // generate WebView to load URL to enter credentials
      ...
    }
};

AuthController authenticator = new AuthControllerImpl(REQUESTING_APP_ID, permissions, language, returnURL, view);
authenticator.signIn();
```

See also: [app authorization in the API reference](http://api.pryv.com/reference/#authorizing-your-app)

### Setup connection

```java
Connection connection = new Connection(userID, accessToken, domain, true, new DBinitCallback());

// Define the scope of the cached data
Filter scope = new Filter();
scope.addStream(myTrackedStream); // Omit this to cache all Pryv data (including data from other apps)
connection.setupCacheScope(scope);
```

### Manage events

#### Retrieve

```java
Filter filter = new Filter(Double from, Double to, Set<Stream> streams, Set<String> tags,
    Set<String> types, Boolean running, Boolean sortAscending, Integer skip, Integer limit,
    State state, Double modifiedSince, String parentId, Boolean includeDeletions, Boolean includeDeletionsSince);

connection.events.get(filter, new GetEventsCallback() {
	@Override
	public void cacheCallback(List<Event> events, Map<String, Double> eventDeletions) {
    	// do something
	}

	@Override
	public void onCacheError(String errorMessage) {
		// do something
	}

	@Override
	public void apiCallback(List<Event> events, Map<String, Double> eventDeletions, Double serverTime) {
		// do something
	}

	@Override
	public void onApiError(String errorMessage, Double serverTime) {
		// do something
	}
});
```

#### Create

```java
Event newEvent = new Event()
newEvent.setStreamId("diary");
newEvent.setType("note/txt");
newEvent.setContent("I track, therefore I am.");
connection.events.create(newEvent, new EventsCallback() {
	@Override
	public void onApiSuccess(String successMessage, Event event, String stoppedId, Double serverTime) {
    	// do something
	}

	@Override
	public void onApiError(String errorMessage, Double serverTime) {
		// do something
	}

	@Override
	public void onCacheSuccess(String successMessage, Event event) {
		// do something
	}

	@Override
	public void onCacheError(String errorMessage) {
		// do something
	}
});
```

#### Update

```java
event.setContent = "Updated content.";
connection.events.update(event, new EventsCallback() {
	@Override
	public void onApiSuccess(String successMessage, Event event, String stoppedId, Double serverTime) {
    	// do something
	}

	@Override
	public void onApiError(String errorMessage, Double serverTime) {
		// do something
	}

	@Override
	public void onCacheSuccess(String successMessage, Event event) {
		// do something
	}

	@Override
	public void onCacheError(String errorMessage) {
		// do something
	}
});
```

#### Delete

```java
connection.events.delete(event, new EventsCallback() {
	@Override
	public void onApiSuccess(String successMessage, Event event, String stoppedId, Double serverTime) {
    	// do something
	}

	@Override
	public void onApiError(String errorMessage, Double serverTime) {
		// do something
	}

	@Override
	public void onCacheSuccess(String successMessage, Event event) {
		// do something
	}

	@Override
	public void onCacheError(String errorMessage) {
		// do something
	}
});
```

### Manage Streams

#### Retrieve

```java
Filter filter = new Filter(Double from, Double to, Set<Stream> streams, Set<String> tags,
    Set<String> types, Boolean running, Boolean sortAscending, Integer skip, Integer limit,
    State state, Double modifiedSince, String parentId, Boolean includeDeletions, Boolean includeDeletionsSince);

connection.streams.get(filter, new GetStreamsCallback() {
	@Override
	public void cacheCallback(Map<String, Stream> streams, Map<String, Double> streamDeletions) {
    	// do something
	}

	@Override
	public void onCacheError(String errorMessage) {
		// do something
	}

	@Override
	public void apiCallback(Map<String, Stream> streams, Map<String, Double> streamDeletions, Double serverTime) {
		// do something
	}

	@Override
	public void onApiError(String errorMessage, Double serverTime) {
		// do something
	}
});
```

#### Create

```java
Stream newStream = new Stream();
newStream.setId("heartRate");
newStream.setName("Heart rate");
connection.streams.create(newStream, new StreamsCallback() {
	@Override
	public void onApiSuccess(String successMessage, Stream stream, Double serverTime) {
		// do something
	}

	@Override
	public void onApiError(String errorMessage, Double serverTime) {
		// do something
	}

	@Override
	public void onCacheSuccess(String successMessage, Stream stream) {
		// do something
	}

	@Override
	public void onCacheError(String errorMessage) {
		// do something
	}
});
```

#### Update

```java
stream.setParentId("health");
connection.streams.update(stream, new StreamsCallback() {
	@Override
	public void onApiSuccess(String successMessage, Stream stream, Double serverTime) {
		// do something
	}

	@Override
	public void onApiError(String errorMessage, Double serverTime) {
		// do something
	}

	@Override
	public void onCacheSuccess(String successMessage, Stream stream) {
		// do something
	}

	@Override
	public void onCacheError(String errorMessage) {
		// do something
	}
});
```

#### Delete

```java
connection.streams.delete(stream, new StreamsCallback() {
	@Override
	public void onApiSuccess(String successMessage, Stream stream, Double serverTime) {
		// do something
	}

	@Override
	public void onApiError(String errorMessage, Double serverTime) {
		// do something
	}

	@Override
	public void onCacheSuccess(String successMessage, Stream stream) {
		// do something
	}

	@Override
	public void onCacheError(String errorMessage) {
		// do something
	}
});
```

### Manage accesses

#### Retrieve

```java
connection.accesses.get(new GetCallback<Access>() {
  @Override
  public void onSuccess(String successMessage, List<Access> accesses, Double serverTime) {
    // do something
  }

  @Override
  public void onError(String errorMessage, Double serverTime) {
    // do something
  }
});
```

#### Create

```java
Access newAccess = new Access();
newAccess.setName("forMyDoctor");
newAccess.addPermission(new Permission("heartRate", Permission.Level.read, null));
connection.accesses.create(newAccess, new CreateCallback<Access>() {
  @Override
  public void onSuccess(String successMessage, Access access, Double serverTime) {
    // do something
  }

  @Override
  public void onError(String errorMessage, Double serverTime) {
    // do something
  }
});

```

#### Update

```java
access.setName("forMyFamily");
connection.accesses.update(access.getId(), access ,new UpdateCallback<Access>() {
  @Override
  public void onSuccess(String successMessage, Access access, Double serverTime) {
    // do something
  }

  @Override
  public void onError(String errorMessage, Double serverTime) {
    // do something
  }
});
```

#### Delete

```java
connection.accesses.delete(access.getId(), new DeleteCallback<Access>() {
  @Override
  public void onSuccess(String successMessage, String id, Double serverTime) {
    // do something
  }

  @Override
  public void onError(String errorMessage, Double serverTime) {
    // do something
  }
});
```

### Batch call

Coming soon!

### Further resources

- [API reference](http://api.pryv.com/reference/)
