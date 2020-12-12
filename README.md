# Old beacon

**ATTENTION**: THIS MOD REQUIRES _FABRIC LOADER_ **AND** _FABRIC_

Get fabric-loader from [here](https://fabricmc.net/use/)

Get fabric from [here](https://www.curseforge.com/minecraft/mc-mods/fabric-api)

---

This is my first ever 'real' minecraft mod.

Adds the old beacon graphics, which had the small netherstar in the center, from the 12w36a snapshot (barring the lightning bolt style beam).

When the beacon is idle the star is blue - this is a feature added by me but if you want to stay true to the original graphics disable it in the config file located at `config/oldbeacon.cfg`, and set `idle_anim=true` to `idle_anim=false`

If you want to change the beacon base so it's like the old style, feel free to use a resource pack. I can't add the old base as an option through the config, as far as I know. I may bundle the resource pack with this mod later.

---

Note that this mod will potentially conflict with ONLY mods that change the beacon rendering code, because I used override mixins for everything.