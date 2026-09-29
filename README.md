# Floatater Classic

Brings back the original Floatater from the 24w14potato April Fools snapshot (the Poisonous Potato Update).

This mod ports the original code to current Minecraft so you can continue building flying machines.

Official Wiki with more info about this block: https://minecraft.wiki/w/Floatater

![A Floatater flying a contraption](https://i.imgur.com/5DXeuiS.png)

## How it works

Placing a Floatater will make it face whatever way you're looking. The front face is the direction it will fly. Powering it using a redstone signal will make it grab everything attached to its front and take off.

Some things you'll run into:
- Every powered Floatater pointing in the same way adds 0.1 blocks per tick, so adding a few together will make you move faster.
- It keeps going until the front of the contraption hits something solid (or the build limit), then puts every block back into the world where it stopped.
- You can ride it. Mobs can also ride it.
- Anything with a block entity (chests, furnaces, signs) breaks off and drops instead of flying.
- Water doesn't come with you.

The size limit is the `floatater_classic:floatater_size_limit` game rule. Default is 32 meaning max size = 32 x 32 x 32 blocks.

## Floatato

You craft Floataters out of Floatatos, but they're useful on their own too. Use one while looking at the sky and it will appear 3 blocks in front of you, hanging in the air. That's the easiest way to start a build off the ground.

## Recipes

Floatato:

![Floatato recipe](https://i.imgur.com/N1a74kj.png)

Floatater:

![Floatater recipe](https://i.imgur.com/G0xjYle.png)

## Versions

| branch | Minecraft | loader |
|---|---|---|
| `26.3` | 26.3 | Fabric |
| `26.3-neoforge` | 26.3 | NeoForge |
| `26.2` | 26.2 | Fabric |
| `26.2-neoforge` | 26.2 | NeoForge |
| `26.1.2` | 26.1.2 | Fabric |
| `26.1.2-neoforge` | 26.1.2 | NeoForge |
| `26.1.1` | 26.1.1 | Fabric |
| `26.1.1-neoforge` | 26.1.1 | NeoForge |
| `26.1` | 26.1 | Fabric |
| `26.1-neoforge` | 26.1 | NeoForge |

Install the mod on both the server and the client (if you play multiplayer). The Fabric builds also need Fabric API. Pick the jar that matches your Minecraft version.

## Credits

The Floatater, the Floatato, their textures and the whole flying-contraption idea are Mojang's, from 24w14potato. I just brought them back.

## License

All rights reserved. Modpacks are welcome: you may include the mod unmodified in any pack, public or private, without asking.
