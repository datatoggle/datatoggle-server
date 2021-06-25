package com.datatoggle.server.api.customer

sealed class Condition;
sealed class Element;

class EventName(): Element()
class Trait(val name: String): Element()
class Property(val name: String): Element()

class AndCondition(val children: List<Condition>): Condition()
class OrCondition(val children: List<Condition>): Condition()
class EqualsCondition<T>(val elt: Element, val equals: T): Condition()
class NotCondition(val child: Condition): Condition()
class LessCondition<T>(val elt: Element, val upperBound: T): Condition()
class MoreCondition<T>(val elt: Element, val lowerBound: T): Condition()
class InCondition<T>(val elt: Element, val values: List<T>): Condition()
