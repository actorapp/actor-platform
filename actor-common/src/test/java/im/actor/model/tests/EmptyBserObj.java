package im.actor.model.tests;

import im.actor.model.droidkit.bser.BserObject;
import im.actor.model.droidkit.bser.BserValues;
import im.actor.model.droidkit.bser.BserWriter;

import java.io.IOException;

/**
 * Created by ex3ndr on 08.03.15.
 */
public class EmptyBserObj extends BserObject {
    @Override
    public void parse(BserValues values) throws IOException {

    }

    @Override
    public void serialize(BserWriter writer) throws IOException {

    }
}
