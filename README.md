# Relaxed JSON Update
[![Build Status](https://travis-ci.org/andyglow/relaxed-json-update.svg)](https://travis-ci.org/andyglow/relaxed-json-update)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.andyglow/relaxed-json-update_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.andyglow/relaxed-json-update_2.11)
[![Download](https://api.bintray.com/packages/andyglow/scala-tools/relaxed-json-update/images/download.svg)](https://bintray.com/andyglow/scala-tools/relaxed-json-update/_latestVersion)

Relaxed (partial) case class update with json.

## Problem
Sometime, especially dealing with http/rest services, we want to have an ability to receive and update only certain
fields of our resource/entity. Let me provide some short example (spray used).
 
Suppose we are working on some Profile API
```scala
case class Profile(id: String, name: String, password: String)
```
So we have already created these scenarios
* `GET    /profiles`      `get    & path("profiles")`
* `GET    /profiles/:id`  `get    & path("profiles" / Segment)`
* `POST   /profiles`      `post   & path("profiles" / Segment) & entity(as[Profile])`
* `DELETE /profiles/:id`  `delete & path("profiles" / Segment)`

And now we are about to implement
* `PUT /profiles/:id`

 > For simplicity of examples let's assume we use **sync** API here, but of course you should think twice,
  how this feet your needs. Almost always you should use **async** approach.

### Step 0
The very first idea that come to our mind is to reuse `Profile` instance like this:
    
```scala
 (put & path("profiles" / Segment) & entity(as[Profile])) { (id, update) =>
   rejectEmptyResponse {
     complete {
       val entity: Option[Profile] = db get id
       for {
         entity <- entity
         updated = entity.copy(
           name = update.name,
           password = update.password)
       } yield {
         db.update(id, updated)
         updated
       } 
     }
   }
 }
```
_Pros & Cons_

* `id` field, which was defined as `String` has to be specified in payload, otherwise unmarshalling will fail.
* We still can't update only certain fields.
  
### Step 1
Make `id` optional
 
```scala
case class Profile(id: Option[String], name: String, password: String)
```

_Pros & Cons_

From one side it resolves issue with necessity to specify `id` twice in `uri` and in payload.

From another
side it brings us to an optional id hell as now we have to deal with it in our services handling it
every time by `getOrElse { throw new IllegalStateException }` or something.
   
And we still can't update partially.

### Step 2
Make Form case class where all fields are optional and `id` field is absent.
 
```scala
case class Profile(id: String, name: String, password: String)
case class ProfileUpdate(name: Option[String], password: Option[String])
```

_Pros & Cons_

* Now you we can omit `id` in payload.
* You can update only necessary fields.
 
But
* you have to have another one class. Just try to imagine how could it look like if you have a rich class with many fields.
* you still have to handle all that fields manually. For example:
```scala
case class ProfileUpdate(name: Option[String], password: Option[String]) {
  def apply(profile: Profile): Profile = {
    val _name = name getOrElse profile.name 
    val _password = password getOrElse profile.password
     
    profile.copy(
      name = _name,
      password = _password) 
  }
}
```
```scala
 (put & path("profiles" / Segment) & entity(as[ProfileUpdate])) { (id, update) =>
   rejectEmptyResponse {
     complete {
       val entity: Option[Profile] = db get id
       for {
         entity <- entity
         updated = update apply entity
       } yield {
         db.update(id, updated)
         updated
       } 
     }
   }
 }
```
This solution is much better but have one significant drawback. You have to write lot of boilerplate code.
Again. Just think about necessity to support this solution having rich class structure. It may become a nightmare.

## Idea
So what exactly this solution does is automate the approach we invented on step 2 by involving scala macros.

1. You don't need to write special `*Update` classes.
2. You don't need to write `copy` boilerplate.

How your code could look like by using this solution:
```scala
 import com.github.andyglow.relaxed._
 import com.github.andyglow.relaxed.PlayJsonSupport._
 
 (put & path("profiles" / Segment) & entity(as[JsValue])) { (id, update) =>
   rejectEmptyResponse {
     complete {
       val entity: Option[Profile] = db get id
       for {
         entity <- entity
         updated = Relaxed(entity) updated update
       } yield {
         db.update(id, updated)
         updated
       } 
     }
   }
 }
```

The same will work for `akka-http` as well.
 
As a bonus there are support for
various popular json libs:
- play-json
- spray-json
- jenkins (scala module)
- circe
- upickle
- argonaut
- json4s
