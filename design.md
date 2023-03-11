Choose a way of making a dropper "spread" its inputted items in a sequential way
- Have a special dropper that can be crafted
- Modify the hopper using an adjacent object e.g. a soul campfire / torch under it
- Tag the dropper as soon as a crafting table is dispensed on to it

Choose a way of creating a mold (filter items that arent included in recipe in dropper)
- Decide a special item to use. e.g. stained glass pane.
  - Stained glass panes aren't used in any recipe for now, but may be in the future
- Dispenser with the CT can include them as a pattern which "blocks" those slots.
  - Wouldn't be able to include more items in dispenser for difficulty
  - Could potentially draw the state using glass panes in the dropper when inspected
    - would need to cancel pick up events, drops, explosions, etc so the glass isnt dropped...

Difficulty / game progress
- Dispenser with crafting table might be too simple / early game for automation
  - Could require a harder-to-obtain item along CT in the dispenser to activate crafting
  - Could have a special type of CT required in dispenser instead of normal CT that is crafted with a hard recipe
    - Crafting table could be distinguished by enchant, lore, name...
      - Doesn't work with localization.
      - Doesn't look good as the item graphic is the same (unless enchanted
      - Feels a bit less vanilla
    - Could have a tier 1 and tier 2 variant, with and without "spreading" behaviour applied to dropper

Item molding
- Items output from dispenser could be a "schematic" or "mold" instead of the actual item.
  - Could the output item be a named shulker box with the items inside?
    - Prevent conversion to regular shulker box
- Molds would be unplaceable item types.
- Molds could be fired in a furnace or blast furnace to be completed.
