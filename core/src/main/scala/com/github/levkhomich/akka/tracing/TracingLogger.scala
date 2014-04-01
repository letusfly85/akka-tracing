/**
 * Copyright 2014 the Akka Tracing contributors. See AUTHORS for more details.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.levkhomich.akka.tracing

import java.util.UUID

import akka.actor.{Actor, DiagnosticActorLogging}
import akka.event.Logging.{InitializeLogger, LogEvent, LoggerInitialized, MDC, emptyMDC}

trait TracingActorLogging extends DiagnosticActorLogging {

  override def mdc(currentMessage: Any): MDC =
    currentMessage match {
      case ts: TracingSupport if ts.span.isDefined =>
        Map(TracingLogger.MessageIdField -> ts.msgId)
      case _ =>
        emptyMDC
    }

}

class TracingLogger extends Actor with ActorTracing {

  def receive = {
    case InitializeLogger(_) =>
      sender() ! LoggerInitialized

    case e: LogEvent =>
      e.mdc.get(TracingLogger.MessageIdField) match {
        case Some(msgId: UUID) =>
          trace.record(msgId, e.getClass.getSimpleName + ": " + e.message)
        case _ =>
          // do nothing
      }
  }

}

private[this] object TracingLogger {
  val MessageIdField = "msgId"
}