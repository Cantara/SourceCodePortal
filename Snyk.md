# Snyk scheduler

## Problem

Snyk.io doesn't provide a push api. They only push to Slack.

Loading time of badges in the in user-agent is time consuming per view. Caching the snyk repo status, would be better for the user if Snyk status could be provided instantly. The use of badges also makes the dashboard look messy.

## Scope

Make a task scheduler that fetches Snyk.io badges and cache SVG objects. The SVG objects contains information about [vulnerabilities](https://snyk.io/docs/badges) and since it is XML the metadata is directly accessible to determine snyk state.

Make a javascript function that loads svg info and update the page with the same look'n'feel as for the rest.

## Solution

Use eviction cache by date-expiry policy and refetch a badge per project when evicted. In this way we can at all times maintain an updated knowledge per repo.

Use Slack Outgoing Webhook App integration to receive notifications through Slack from Snyk.
 
--

Questions:

* Do we need instant information?
* should this be delegated to a jquery or backbonejs to handling data loading?
