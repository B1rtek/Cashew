# Cashew
A fun Discord bot with lots of cool commands, themed around the visual novel series Nekopara

## Features
- `/casesim` - CS:GO case opening simulator with inventory
- `/counting` - a counting game, write the next number or an expression that evaluates to it
- `/poll` - a command that creates polls, which display a piechart with results after the poll ends
- `/scheduler` - a command to schedule messages for the bot to send them each day at the set time
- `/when` - a system allowing for creation of your own rules, based on triggers such as member reacting to a message, and actions - adding a role to the member who reacted
- `/reminder` - schedule reminders for yourself
- `/birthday` - let everyone on the server know that it's your birthday (and make the bot wish you happy birthday too)
- Roleplay commands featuring gifs from the Nekopara anime
- And more (and the list is growing!)

## Inviting to your server
Right now it's not available to everyone, but that will soon change. You can join one of the servers where Cashew was already added to try it out, such as [Nekord](https://discord.gg/EVcdmJuM). If you want to invite it, DM me on Discord (B1rtek#2383)

## Running it yourself
Currently there is no way to run Cashew yourself because there is a bit of code missing that would create crucial tables in the database. That will be fixed soon, and the only requirements to run Cashew will be:
- Bot with server members and message content intents
- Discord API key for a bot
- A Postgres database

You'll need to set two environment variables:
- `TOKEN` - your API key
- `JDBC_DATABASE_URL` - JDBC URL for the postgres database
After that, create a JAR with the stage task and run the compiled JAR

## Support
If you encounter any issues, you can:
- Contact me on Discord (B1rtek#2383) to tell me about the issue
- Send a `/feedback` about the issue
- Open a new issue on github describing the problem
Issues can be used to suggest new features too
To see what is currently being worked on, see [Cashew's Trello board](https://trello.com/b/R432WEsW/cashew-bot)

## If you really like Cashew
[You can buy me a tea :)](https://ko-fi.com/b1rtek)
