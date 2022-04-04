# Java k.LAB API Client

<img src="https://docs.integratedmodelling.org/klab/_images/KLAB_LOGO.png" align="right"
     alt="k.LAB logo" width="258" height="108">

This package is a Java client for [k.LAB](https://github.com/integratedmodelling/klab). It allows registered users of k.LAB to make
observations on the k.LAB semantic web from a Java program using the REST API. Similar clients are being written for Python and Javascript.

## Installation 

This package is available in Maven Central as `integratedmodelling.org:klab-api`. To add to a Maven project, add

```maven
<dependency>
   <groupId>org.integratedmodelling</groupId>
   <artifactId>klab-api</artifactId>
   <version>${klab.api.version}</version>
</dependency>
```
to the dependency section of your `pom.xml`. The latest version is **0.1.0-SNAPSHOT**. The only dependencies are the core k.LAB
interface package and Unirest.

## Use of the k.LAB network

## Outputs

### States

### Objects and groups

### Reports

### Dataflows

## Usage

### Code examples

The k.LAB API main function is to make _observations_ of _concepts_ within a "root" observation 
called the _context_. The API provides peer objects for the Klab service and for observations.
Contexts and observations can be produced in two ways:

* directly, i.e. sending the request to the API without making an estimate of the cost first;
* indirectly, obtaining an estimate with the likely amount of work needed for the task which can 
  be later submitted for computation.
  
The simplest case is a direct observation. Every time an API request is made, a ticket is returned by
the API. The API user must poll the ticket until it is reported as resolved or aborted. The Java 
client automates this using Java futures.  

<details><summary><b>Create a context and make a simple observation</b></summary>

```java
// connect to the server at the passed URL. Calling create() without parameters will
// connect to a local engine, which must be active.
Klab klab = Klab.create("https://my.klab.engine.com", "myusername", "mypassword");

// a Context extends Observation with methods that enable making observations in it and
// querying them
Context context = klab
		.submit(
			Observable.create("earth:Region"), 
			Geometry.builder().grid(ruaha, "1 km").years(2010).build())
		// submit() returns a task; calling get() on it blocks until the observation is made
		.get();

// remote failure will result in exceptions thrown by get(), so if we get here we have a valid context
Observation elevation = context.submit(new Observable("geography:Elevation")).get();

// in the passed context, the observation of elevation should be between 270 and 2800 m
assert Range.create(0, 3000).contains(elevation.getDataRange());

/*
 * ensure the context has been updated with the new observation
 */
assert context.getObservation("elevation") instanceof Observation;
```
</details>

## Who Uses the k.LAB Java API

## License

## See also
