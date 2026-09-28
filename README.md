# Floatater

Brings back the Floatater from the 24w14potato April Fools snapshot (the Poisonous Potato Update): a redstone engine that picks up a whole contraption and flies it until it hits something.

## How it works

- Place a Floatater. It faces the way you look, and its front face is the direction it will fly.
- Power it with redstone. One tick later it grabs every block connected to its front and flies the lot forward.
- Connectivity follows the snapshot: blocks whose touching faces overlap are joined, slime and honey stick on every side, sticky pistons stick on their face, and a Floatater only sticks on its front. The block directly in front is always pushed, even if its shape does not touch.
- Every powered Floatater facing the same way adds 0.1 blocks per tick of speed.
- The contraption stops when its leading edge would hit a block that cannot be replaced, or the build limit, and is placed back into the world.
- Players and mobs standing on it ride along. Flying players do not.
- Blocks with block entities (chests, furnaces, signs...) break off and drop instead of being carried, just like in the snapshot.

The `floatater:floatater_size_limit` game rule (default 32) caps a contraption at N x N x N blocks. Anything bigger does not move.

## Floatato

The Floatato is the Floatater's crafting ingredient, and like in the snapshot you can place it in mid air: use it while not looking at a block and it appears 3 blocks in front of you.

## Recipes

- Floatato (x8): 8 poisonous potatoes around a ghast tear.
- Floatater: poisonous potatoes on top, a baked potato in the middle flanked by Floatatos, and a row of Floatatos at the bottom. The snapshot used its own Hot Potato here, which does not exist in the real game, so this mod uses a baked potato instead.

## Versions

| branch | minecraft | loader |
|---|---|---|
| `26.3` | 26.3 | Fabric |
| `26.3-neoforge` | 26.3 | NeoForge |

Both the server and the client need the mod.

## Credits

The Floatater, Floatato, their textures and the moving-grid mechanics are Mojang's, from the 24w14potato snapshot. This mod ports them to current Minecraft.

## License

This mod is distributed under the [CC0-1.0 License](LICENSE).
