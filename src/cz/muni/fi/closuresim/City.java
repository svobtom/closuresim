package cz.muni.fi.closuresim;

/**
 *
 * @author Tom
 */
public class City extends Node {
    private int numOfInhabitants;
    private String name;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int getNumOfInhabitant() {
        return numOfInhabitants;
    }

    @Override
    public void setNumOfInhabitants(int numOfInhabitants) {
        this.numOfInhabitants = numOfInhabitants;
    }
}
