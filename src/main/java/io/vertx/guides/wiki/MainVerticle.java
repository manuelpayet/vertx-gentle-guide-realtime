/*
 *  Copyright (c) 2017 Red Hat, Inc. and/or its affiliates.
 *  Copyright (c) 2017 INSA Lyon, CITI Laboratory.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.vertx.guides.wiki;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import io.reactivex.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.Vertx;

/**
 * @author <a href="https://julien.ponge.org/">Julien Ponge</a>
 */
public class MainVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> promise) throws Exception {

    // tag::rx-deploy-verticle[]
    Single<String> dbVerticleDeployment = vertx.rxDeployVerticle(
      "io.vertx.guides.wiki.database.WikiDatabaseVerticle");
    // end::rx-deploy-verticle[]

    // tag::rx-sequential-composition[]
    dbVerticleDeployment
      .flatMap(id -> { // <1>

        Single<String> httpVerticleDeployment = vertx.rxDeployVerticle(
          "io.vertx.guides.wiki.http.HttpServerVerticle",
          new DeploymentOptions().setInstances(2));

        return httpVerticleDeployment;
      })
      .subscribe(id -> promise.complete(), promise::fail); // <3>
    // end::rx-sequential-composition[]
  }
  
  public static void main(String[] args) throws InterruptedException {
	  BlockingQueue<AsyncResult<String>> q = new ArrayBlockingQueue<>(1);
	  Vertx.vertx().deployVerticle(new MainVerticle(), q::offer);
	  AsyncResult<String> result = q.take();
	  if (result.failed()) {
	      throw new RuntimeException(result.cause());
	  }
}
}
