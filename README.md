# Floatater

Brings back the Floatater from the 24w14potato April Fools snapshot (the Poisonous Potato Update).

It only existed for that one snapshot. This mod ports the original code to current Minecraft so you can keep building flying machines.

## How it works

Place a Floatater and it faces whichever way you're looking. That front face is the direction it flies. Give it a redstone signal and a tick later it grabs everything attached to its front and takes off with it.

"Attached" works the same way it did in the snapshot. Blocks stick together when their touching faces overlap. Slime and honey stick on every side, a sticky piston sticks on its face, and a Floatater only sticks on its front. Whatever sits directly in front of it gets pushed along too, even if the shapes don't touch.

Some things you'll run into:

- Every powered Floatater pointing the same way adds 0.1 blocks per tick, so stack a few if you're in a hurry.
- It keeps going until the front of the contraption hits something solid (or the build limit), then puts every block back into the world where it stopped.
- You can ride it, and so can mobs. If you're flying in creative, it leaves you behind.
- Anything with a block entity (chests, furnaces, signs) breaks off and drops instead of flying. Annoying, but the snapshot did the same.
- Water doesn't come with you. Waterlogged blocks fly dry.
- Keep your build off the ground. It grabs everything connected to it, and if that adds up to more than the size limit, nothing moves.

The size limit is the `floatater:floatater_size_limit` game rule. It defaults to 32, so a contraption can be at most 32 x 32 x 32 blocks.

## Floatato

You craft Floataters out of Floatatos, but they're useful on their own too. Use one while looking at nothing and it appears 3 blocks in front of you, hanging in the air. That's the easiest way to start a build off the ground.

## Recipes

- Floatato (makes 8): 8 poisonous potatoes around a ghast tear.
- Floatater: poisonous potatoes across the top, then Floatato, baked potato, Floatato in the middle row, and three Floatatos along the bottom.

The snapshot put a Hot Potato in the middle of that recipe. Hot Potatoes never made it into the real game, so a baked potato stands in for it.

## Versions

| branch | Minecraft | loader |
|---|---|---|
| `26.3` | 26.3 | Fabric |
| `26.3-neoforge` | 26.3 | NeoForge |
| `26.2` | 26.2 | Fabric |
| `26.2-neoforge` | 26.2 | NeoForge |

Install the mod on both the server and the client. The Fabric builds also need Fabric API.

## Credits

The Floatater, the Floatato, their textures and the whole flying-contraption idea are Mojang's, from 24w14potato. I just brought them back.

## License

All rights reserved. Modpacks are welcome: you may include the mod unmodified in any pack, public or private, without asking.
