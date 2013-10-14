# Proctor Demo and Reference Implementation

This project is a reference implementation and demonstration of [Proctor](https://github.com/indeedeng/proctor), a Java-based A/B testing framework by [Indeed](http://engineering.indeed.com). It is a simple Spring MVC webapp that uses Proctor to determine what background color to set for a web application.

For more information, see the [Proctor documentation](http://indeedeng.github.io/proctor). The [Quick Start](http://indeedeng.github.io/proctor/docs/quick-start/) document is a good guide to understanding the code in this reference implementation.

## Demo Online

The demo is running on heroku: [http://indeedeng-hello-proctor.herokuapp.com](http://indeedeng-hello-proctor.herokuapp.com). It loads its [test definitions](http://indeedeng.github.io/proctor/docs/terminology/#toc_4) from JSON files posted as GISTs on github. A URL parameter allows you to change the definition file.

### Things to Try

1. Testing two colors at 25% and 25%: [link](http://indeedeng-hello-proctor.herokuapp.com/?defn=https%3A%2F%2Fgist.github.com%2Fyouknowjack%2F6771052%2Fraw).  Click "Show Details" and "Reset" to change your user ID and (possibly) be put in a different test bucket.

1. Testing the same two colors at 50% and 50%: [link](http://indeedeng-hello-proctor.herokuapp.com/?defn=https%3A%2F%2Fgist.github.com%2Fyouknowjack%2F6718854%2Fraw)

1. Going to 100% for one color: [link](http://indeedeng-hello-proctor.herokuapp.com/?defn=https%3A%2F%2Fgist.github.com%2Fyouknowjack%2F6718870%2Fraw)

1. Using `prforceGroups` to see a different test group, regardless of allocations: [link](http://indeedeng-hello-proctor.herokuapp.com/?prforceGroups=bgcolortst3)

1. Reset to discard the forced group in the previous step: [link](http://indeedeng-hello-proctor.herokuapp.com/reset)

1. Basing color on Android vs. iOS user agent instead of random allocation: [link](http://indeedeng-hello-proctor.herokuapp.com/?defn=https%3A%2F%2Fgist.github.com%2Fyouknowjack%2F6718801%2Fraw). If you're not on Android or iOS you won't see a background color.

#### Web-Based Remote Service API

An additional endpoint `/rpc` is provided in this implementation as an example of how you might implement group selection as a remote service. To use this endpoint, you must provide as least the `uid` (user ID) and `agent` (user agent) query parameters. It does not use any cookies or HTTP headers directly. It supports these parameters:
<table>
<tr>
<td>Parameter</td>
<td>Description</td>
<td>Required?</td>
<td>Example</td>
</tr>
<tr>
<td>uid</td>
<td>User ID for USER-based tests (can be any string)</td>
<td>Yes</td>
<td>8ac65ba448be45afb86706e8cab979cf</td>
</tr>
<tr>
<td>agent</td>
<td>User Agent (equivalent to User-Agent HTTP header)</td>
<td>Yes (may be blank)</td>
<td>Mozilla/5.0</td>
</tr>
<tr>
<td>defn</td>
<td>Definition URL</td>
<td>No (uses default if not provided)</td>
<td>https://gist.github.com/youknowjack/6549712/raw</td>
</tr>
</table>

## Building and Running Demo Locally

1. `mvn clean package && java -jar target/dependency/webapp-runner.jar target/*.war`

1. Go to [http://localhost:8080/](http://localhost:8080/)

## The Source

### [ProctorGroups.json](https://github.com/indeedeng/proctor-demo/blob/master/src/main/proctor/com/indeed/demo/ProctorGroups.json)
The JSON specification that is enumerates the test and its buckets. This is used to generate convenience classes at compile time and to load the test matrix at runtime.

### [DefinitionManager.java](https://github.com/indeedeng/proctor-demo/blob/master/src/main/java/com/indeed/demo/proctor/DefinitionManager.java)
Helper component that manages loading and caching the test matrix from a definition file at a remote URL.

### [DemoController.java](https://github.com/indeedeng/proctor-demo/blob/master/src/main/java/com/indeed/demo/proctor/DemoController.java)
Spring controller that handles assigning a UUID cookie to identify the user and calling into proctor to get the groups for the current user. Also provides `/rpc` service endpoint support.

### [demo.jsp](https://github.com/indeedeng/proctor-demo/blob/master/src/main/webapp/WEB-INF/jsp/demo.jsp)
Java Servlet Page view for the demo controller; renders the test behavior and some controls to interact with the demo.

### [UserAgent.java](https://github.com/indeedeng/proctor-demo/blob/master/src/main/java/com/indeed/web/useragents/UserAgent.java)
A helper class based partially on bitwalker's UserAgentUtils that can be a useful context parameter for proctor.


[![Bitdeli Badge](https://d2weczhvl823v0.cloudfront.net/indeedeng/proctor-demo/trend.png)](https://bitdeli.com/free "Bitdeli Badge")

